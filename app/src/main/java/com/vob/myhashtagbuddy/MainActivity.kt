package com.vob.myhashtagbuddy

import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.vob.myhashtagbuddy.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var animationDrawable: AnimationDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        animationDrawable = binding.main.background as AnimationDrawable
        animationDrawable.setExitFadeDuration(400)
    }

    override fun onResume() {
        super.onResume()
        animationDrawable.start()
    }
}