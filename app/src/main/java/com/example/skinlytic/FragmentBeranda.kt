package com.example.skinlytic

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.skinlytic.MainActivity
import com.example.skinlytic.Deteksi.FragmentDeteksi
import com.example.skinlytic.Deteksi.FragmentDeteksi2
import com.example.skinlytic.Tentang.FragmentTentang
import com.example.skinlytic.databinding.ActivityMainBinding
import com.example.skinlytic.databinding.FragmentBerandaBinding

class FragmentBeranda : Fragment() {

    private var _binding: FragmentBerandaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentBerandaBinding.inflate(inflater, container, false)

        setUpGridMenu(binding)

        return binding.root
    }
    private fun setUpGridMenu(binding: FragmentBerandaBinding) {

        binding.inclueHomeGrid.cardMenu1.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(FragmentDeteksi())
        }

        binding.inclueHomeGrid.cardMenu2.setOnClickListener {
            (activity as? MainActivity)?.replaceFragment(FragmentTentang())
        }
    }
}