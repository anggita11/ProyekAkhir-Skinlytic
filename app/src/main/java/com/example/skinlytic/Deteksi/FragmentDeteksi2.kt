package com.example.skinlytic.Deteksi

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.skinlytic.R
import com.example.skinlytic.databinding.FragmentDeteksi2Binding
import com.example.skinlytic.databinding.FragmentDeteksiBinding
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.concurrent.thread

class FragmentDeteksi2 : Fragment() {

    private lateinit var binding: FragmentDeteksi2Binding
    private var tflite: Interpreter? = null
    private var selectedBitmap: Bitmap? = null
    private var photoUri: Uri? = null

    private val CAMERA_REQUEST_CODE = 1
    private val GALLERY_REQUEST_CODE = 2
    private val labels = arrayOf("MEL", "NV", "BCC", "AK", "BKL", "DF", "VASC", "SCC", "UNK")
    private val imageSize = 224

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeteksi2Binding.inflate(layoutInflater)
        setHasOptionsMenu(true)

        // Toolbar setup
        binding.toolbar.title = "Deteksi Penyakit"
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.navigationIcon?.setColorFilter(
            ContextCompat.getColor(requireContext(), R.color.white),
            PorterDuff.Mode.SRC_ATOP
        )

        // Load TFLite model di background thread
        thread {
            try {
                val modelBuffer = FileUtil.loadMappedFile(requireContext(), "modelvit.tflite")
                val options = Interpreter.Options().apply { setNumThreads(4) }
                tflite = Interpreter(modelBuffer, options)

                // ✅ Resize input tensor biar match dengan [1,3,224,224]
                tflite?.resizeInput(0, intArrayOf(1, 3, imageSize, imageSize))
                tflite?.allocateTensors()

                // Debug input/output
                val inputTensor = tflite!!.getInputTensor(0)
                Log.d("TFLiteDebug", "Model expects input: ${inputTensor.shape().joinToString()}")

                val outputTensor = tflite!!.getOutputTensor(0)
                Log.d("TFLiteDebug", "Output shape: ${outputTensor.shape().joinToString()}")

                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Model berhasil dimuat", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Gagal memuat model: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        // Gallery button
        binding.btnupload.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            startActivityForResult(intent, GALLERY_REQUEST_CODE)
        }

        // Detect button
        binding.btndeteksi.setOnClickListener {
            if (tflite == null) {
                Toast.makeText(requireContext(), "Model belum siap", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            selectedBitmap?.let { bitmap ->
                thread {
                    val (label, prob) = runModel(bitmap)
                    requireActivity().runOnUiThread {
                        binding.tvHasilDeteksi.text =
                            "Hasil: $label (Prob: ${"%.2f".format(prob * 100)}%)"
                    }
                }
            } ?: run {
                binding.tvHasilDeteksi.text = "Pilih gambar dulu"
            }
        }

        return binding.root
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile = File.createTempFile(
            "IMG_",
            ".jpg",
            requireContext().getExternalFilesDir("Pictures")
        )
        photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            photoFile
        )
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                photoUri?.let { uri ->
                    Glide.with(this).load(uri).into(binding.imageView)
                    val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
                    selectedBitmap = bitmap

                    // 🔥 Auto deteksi langsung
                    thread {
                        val (label, prob) = runModel(bitmap)
                        requireActivity().runOnUiThread {
                            binding.tvHasilDeteksi.text =
                                "Hasil: $label (Prob: ${"%.2f".format(prob * 100)}%)"
                        }
                    }
                }
            }
            GALLERY_REQUEST_CODE -> {
                data?.data?.let { uri ->
                    Glide.with(this).load(uri).into(binding.imageView)
                    val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
                    selectedBitmap = bitmap

                    // 🔥 Auto deteksi langsung
                    thread {
                        val (label, prob) = runModel(bitmap)
                        requireActivity().runOnUiThread {
                            binding.tvHasilDeteksi.text =
                                "Hasil: $label (Prob: ${"%.2f".format(prob * 100)}%)"
                        }
                    }
                }
            }
        }
    }

    private fun runModel(bitmap: Bitmap): Pair<String, Float> {
        val resized = Bitmap.createScaledBitmap(bitmap, imageSize, imageSize, true)

        // ByteBuffer untuk [1,3,H,W] (channel-first)
        val inputBuffer = ByteBuffer.allocateDirect(1 * 3 * imageSize * imageSize * 4).apply {
            order(ByteOrder.nativeOrder())
        }

        val pixels = IntArray(imageSize * imageSize)
        resized.getPixels(pixels, 0, imageSize, 0, 0, imageSize, imageSize)

        // Isi buffer channel-first
        for (c in 0 until 3) {
            for (y in 0 until imageSize) {
                for (x in 0 until imageSize) {
                    val pixel = pixels[y * imageSize + x]
                    val value = when (c) {
                        0 -> ((pixel shr 16 and 0xFF) / 255.0f - 0.5f) / 0.5f // R
                        1 -> ((pixel shr 8 and 0xFF) / 255.0f - 0.5f) / 0.5f  // G
                        else -> ((pixel and 0xFF) / 255.0f - 0.5f) / 0.5f     // B
                    }
                    inputBuffer.putFloat(value)
                }
            }
        }

        val output = Array(1) { FloatArray(labels.size) }
        tflite?.run(inputBuffer, output)

        val maxIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1
        return labels[maxIndex] to output[0][maxIndex]
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                requireActivity().onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}