package com.vob.myhashtagbuddy

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.content.ClipboardManager
import android.view.View
import android.widget.Toast
import androidx.core.view.isEmpty
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.pranavpandey.android.dynamic.toasts.DynamicToast
import com.rbddevs.splashy.Splashy
import com.vob.myhashtagbuddy.databinding.ActivityMainBinding
import com.vob.myhashtagbuddy.util.Constants.IMAGE_PICK_CODE

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var animationDrawable: AnimationDrawable
    private lateinit var copyText: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSplashy()

        copyText = "";

//        animationDrawable = binding.main.background as AnimationDrawable
//        animationDrawable.setExitFadeDuration(400)


        binding.mainFAB.setOnClickListener {
            copyText = ""
            pickImage()
        }

        if (!binding.mainChipGroup.isEmpty())
        {
            binding.copyButton.visibility = View.VISIBLE
        }

        binding.copyButton.setOnClickListener {
            if (copyText.isNotBlank() && copyText.isNotEmpty())
            {
                val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("label", copyText)
                clipboardManager.setPrimaryClip(clipData)
                DynamicToast.makeSuccess(this,"Copied to clipboard", Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun setSplashy() {
        Splashy(this)
            .setLogo(R.drawable.transparent_logo)
            .setTitle("MyHashtagBuddy")
            .setTitleColor("#FFFFFF")
            .setAnimation(Splashy.Animation.GROW_LOGO_FROM_CENTER)
            .showProgress(true)
            .setBackgroundColor(R.color.black)
            .setDuration(3500L)
            .setProgressColor("#FFFFFF")
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK)
        {
           when(requestCode) {
               IMAGE_PICK_CODE -> {
                   val bitmap = getImageFromData(data)
                   bitmap.apply {
                       binding.mainIV.setImageBitmap(this)
                       this?.let { labelImage(it) }
                   }
               }
           }
        }
    }

    private fun labelImage(bitmap: Bitmap) {
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(bitmap, 0)
        labeler.process(image)
            .addOnSuccessListener { labelList ->
                // Task completed successfully
                // ...
                binding.mainChipGroup.removeAllViews()
                labelList.sortedByDescending { firebaseVisionImageLabel ->
                    firebaseVisionImageLabel.confidence
                }.map { visionImageLabel ->
                    Chip(this, null, R.style.Widget_MaterialComponents_Chip_Choice)
                        .apply { text = visionImageLabel.text }
                }
                    .forEach {
                        binding.mainChipGroup.addView(it)
                        binding.copyButton.visibility = View.VISIBLE
                        copyText += "#"+it.text+" "
                    }

                if (binding.mainChipGroup.isEmpty())
                {
                    binding.copyButton.visibility = View.GONE
                    Snackbar.make(binding.root,"No hashtags could be generated",Snackbar.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                exception.message?.let {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                }
                binding.copyButton.visibility = View.GONE
            }
    }


    private fun getImageFromData(data: Intent?): Bitmap? {
        val selectedImage = data?.data
        return MediaStore.Images.Media.getBitmap(
            this.contentResolver,
            selectedImage
        )
    }



    private fun pickImage() {
        val intent = Intent().apply {
            action = Intent.ACTION_PICK
            type = "image/*"
        }
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),IMAGE_PICK_CODE)
    }



    override fun onResume() {
        super.onResume()

    }
}