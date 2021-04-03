package com.vob.myhashtagbuddy

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
//import com.google.firebase.ml.vision.FirebaseVision
//import com.google.firebase.ml.vision.common.FirebaseVisionImage
//import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.vob.myhashtagbuddy.databinding.ActivityMainBinding
import com.vob.myhashtagbuddy.util.Constants.IMAGE_PICK_CODE
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var animationDrawable: AnimationDrawable
    private var isText = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        animationDrawable = binding.main.background as AnimationDrawable
        animationDrawable.setExitFadeDuration(400)

        binding.mainSwitch.setOnClickListener {
            isText = binding.mainSwitch.isChecked
        }

        binding.mainFAB.setOnClickListener {
            pickImage()
        }

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
//                       if (!isText)
//                           bitmap?.let { processImageTagging(it) }
//                       else
//                           bitmap?.let { startTextRecognisation(it) }
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
                    }
            }
            .addOnFailureListener { exception ->
                exception.message?.let {
                    Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                }
            }
    }

//    private fun processImageTagging(bitmap: Bitmap) {
//        val visionImage = FirebaseVisionImage.fromBitmap(bitmap)
//        FirebaseVision.getInstance().onDeviceImageLabeler.processImage(visionImage)
//            .addOnSuccessListener { labelList ->
//                binding.mainChipGroup.removeAllViews()
//                labelList.sortedByDescending { firebaseVisionImageLabel ->
//                    firebaseVisionImageLabel.confidence
//                }.map { visionImageLabel ->
//                    Chip(this, null, R.style.Widget_MaterialComponents_Chip_Choice)
//                        .apply { text = visionImageLabel.text }
//                }
//                    .forEach {
//                        binding.mainChipGroup.addView(it)
//                    }
//            }
//            .addOnFailureListener { exception ->
//                exception.message?.let {
//                    Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
//                }
//            }
//    }

    private fun getImageFromData(data: Intent?): Bitmap? {
        val selectedImage = data?.data
        return MediaStore.Images.Media.getBitmap(
            this.contentResolver,
            selectedImage
        )
    }

//    private fun startTextRecognisation(bitmap: Bitmap) {
//        if (binding.mainIV.drawable != null) {
//            val image = FirebaseVisionImage.fromBitmap(bitmap)
//            val detector = FirebaseVision.getInstance().onDeviceTextRecognizer
//            detector.processImage(image)
//                .addOnSuccessListener { firbaseVisionText ->
//                    processTextBlock(firbaseVisionText)
//                }
//                .addOnFailureListener {
//                    it.message?.let { it1 ->
//                        Snackbar.make(binding.root, it1, Snackbar.LENGTH_LONG).show()
//                    }
//                }
//        }
//        else
//        {
//            Toast.makeText(this, "Failed", Toast.LENGTH_LONG).show()
//        }
//    }
//
//    private fun processTextBlock(firbaseVisionText: FirebaseVisionText?) {
//        binding.mainChipGroup.removeAllViews()
//        firbaseVisionText?.textBlocks?.map {
//            Chip(this, null, R.style.Widget_MaterialComponents_Chip_Choice)
//                .apply { text = it.text }
//        }?.forEach {
//            binding.mainChipGroup.addView(it)
//        }
//    }

    private fun pickImage() {
        val intent = Intent().apply {
            action = Intent.ACTION_PICK
            type = "image/*"
        }
        startActivityForResult(Intent.createChooser(intent, "Select Picture"),IMAGE_PICK_CODE)
    }



    override fun onResume() {
        super.onResume()
        animationDrawable.start()
    }
}