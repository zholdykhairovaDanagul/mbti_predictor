package kz.application.mbti.predictor

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_image_prediction.*
import kotlinx.android.synthetic.main.layout_result.*
import kotlinx.android.synthetic.main.layout_text_prediction.*
import kz.application.mbti.predictor.predictor.ImagePredictor
import kz.application.mbti.predictor.predictor.TextPredictor
import java.io.IOException

private const val CAMERA = 50001
private const val GALLERY = 50002
private const val WAITING_TIME = 2000L // MILLISECONDS

class MainActivity : AppCompatActivity() {
    private enum class Type {
        IMAGE,
        TEXT
    }

    private var currentFunction = Type.TEXT
    private var predictedType: String? = null
    private var changedTextCount: Int = 0
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        btnPredict.setOnClickListener {
//
//            hideSoftKeyboard(this)
//            if (isInputTextValid(etInputText.text.toString())) {
//                if (changedTextCount >= 20) {
//                    changedTextCount = 0
//                    val predictor =
//                        TextPredictor(
//                            assets,
//                            "word_dict.json",
//                            etInputText.text.toString()
//                        )
//                    predictor.setCallback(object : TextPredictor.DataCallback {
//                        override fun onDataProcessed(result: HashMap<String, Int>?) {
//                            pbLoading.visibility = View.VISIBLE
//                            llResult.visibility = View.INVISIBLE
//                            incResult.visibility = View.VISIBLE
//                            Handler().postDelayed({
//                                pbLoading.visibility = View.GONE
//                                llResult.visibility = View.VISIBLE
//                                predictedType = result?.keys.toString()
//                                predictedType?.let { type -> setPredictedType(type) }
//                            }, WAITING_TIME)
//
//                        }
//                    })
//                    predictor.loadData()
//                }
//            }
//        }


        etInputText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                changedTextCount += s.toString().length
//                btnPredict.isClickable = changedTextCount >= 20
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
        btnReadAboutType.setOnClickListener {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("MBTI_TYPE", predictedType)
            startActivity(intent)
        }

        // Underline read about type
        val content = SpannableString(getString(R.string.read_about_type))
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        btnReadAboutType.text = content

        btnUploadImage.setOnClickListener {
            Dexter.withActivity(this)
                .withPermissions(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        token?.continuePermissionRequest()
                    }

                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        selectImage(this@MainActivity)
                    }
                })
                .check()

        }

        configureBottomNavigation()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            return
        }
        when (requestCode) {
            CAMERA -> {
                CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON).start(this)
            }
            GALLERY -> {
                try {
                    CropImage.activity(data?.data).setGuidelines(CropImageView.Guidelines.ON)
                        .start(this)
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val res = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    val resultUri = res.uri
                    onImageLoaded(resultUri)
                }
            }
        }
    }

    private fun isInputTextValid(text: String): Boolean {
        if (text.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_empty_text), Toast.LENGTH_SHORT).show()
            YoYo.with(Techniques.Shake)
                .duration(300)
                .playOn(etInputText)
            return false
        } else {
            if (text.length < 100) {
                Toast.makeText(this, getString(R.string.error_min_10), Toast.LENGTH_SHORT).show()
                YoYo.with(Techniques.Shake)
                    .duration(300)
                    .playOn(etInputText)
                return false
            }
        }
        return true
    }

    private fun setPredictedType(type: String) {
        tvPredictedType.text = type
        YoYo.with(Techniques.FadeIn)
            .duration(600)
            .playOn(tvPredictedType)
    }

    private fun selectImage(context: Context) {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose your profile picture")

        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Take Photo" -> {
                    val contentValues = ContentValues()
                    contentValues.put(MediaStore.Images.Media.TITLE, "newPic")
                    contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Image to text")
                    imageUri = contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    )

                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    startActivityForResult(intent, CAMERA)

                }
                options[item] == "Choose from Gallery" -> {
                    val galleryIntent = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )

                    startActivityForResult(galleryIntent, GALLERY)

                }
                options[item] == "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun onImageLoaded(bitmap: Bitmap?) {
        if (bitmap != null) {
            pbLoading.visibility = View.VISIBLE
            llResult.visibility = View.INVISIBLE
            incResult.visibility = View.VISIBLE

            Handler().postDelayed({
                pbLoading.visibility = View.GONE
                llResult.visibility = View.VISIBLE
                predictedType = ImagePredictor.predict(bitmap, assets)
                predictedType?.let { type -> setPredictedType(type) }

            }, WAITING_TIME)

        } else {
            Toast.makeText(this@MainActivity, "Failed to load photo", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onImageLoaded(uri: Uri?) {
        ivSelectedImage.setImageURI(uri)
        onImageLoaded((ivSelectedImage.drawable as BitmapDrawable).bitmap)
    }

    private fun hideSoftKeyboard(activity: Activity) {
        try {
            val inputMethodManager = activity.getSystemService(
                Activity.INPUT_METHOD_SERVICE
            ) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(
                activity.currentFocus!!.windowToken, 0
            )
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun configureBottomNavigation() {
        ivTextFragment.setOnClickListener {
            incText.visibility = View.VISIBLE
            incImage.visibility = View.GONE
            if (currentFunction == Type.IMAGE)
                incResult.visibility = View.GONE
            currentFunction = Type.TEXT
            ivTextFragment.setColorFilter(
                ContextCompat.getColor(this, R.color.colorRed),
                PorterDuff.Mode.SRC_IN
            )
            ivImageFragment.setColorFilter(
                ContextCompat.getColor(this, R.color.colorBlack),
                PorterDuff.Mode.SRC_IN
            )
        }

        ivImageFragment.setOnClickListener {
            incText.visibility = View.GONE
            incImage.visibility = View.VISIBLE
            if (currentFunction == Type.TEXT)
                incResult.visibility = View.GONE
            currentFunction = Type.IMAGE

            ivTextFragment.setColorFilter(
                ContextCompat.getColor(this, R.color.colorBlack),
                PorterDuff.Mode.SRC_IN
            )
            ivImageFragment.setColorFilter(
                ContextCompat.getColor(this, R.color.colorRed),
                PorterDuff.Mode.SRC_IN
            )
        }
    }

}
