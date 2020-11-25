package kz.application.mbti.predictor.fragments


import android.Manifest
import android.animation.Animator
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.fragment_image.*
import kotlinx.android.synthetic.main.fragment_image.view.*
import kotlinx.android.synthetic.main.layout_image_prediction.*
import kotlinx.android.synthetic.main.layout_image_prediction.view.*
import kotlinx.android.synthetic.main.layout_text_prediction.btnPredict
import kz.application.mbti.predictor.R
import kz.application.mbti.predictor.listener.OnFragmentActivityListener

const val CAMERA = 50001
const val GALLERY = 50002

class ImageFragment : Fragment() {

    private var listener: OnFragmentActivityListener? = null
    private var imageUri: Uri? = null
    private lateinit var ivSelectedImage: ImageView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as OnFragmentActivityListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_image, container, false)
        view.btnPredict.setOnClickListener {
            YoYo.with(Techniques.Pulse)
                .duration(300)
                .repeat(3)
                .playOn(view.btnPredict)

            showLoading()
            Handler().postDelayed({
                YoYo.with(Techniques.ZoomOut).duration(500)
                    .withListener(object : Animator.AnimatorListener {
                        override fun onAnimationRepeat(animation: Animator?) {

                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            hideLoading()
                            listener?.onPredictionResult("ENTP")
                        }

                        override fun onAnimationCancel(animation: Animator?) {

                        }

                        override fun onAnimationStart(animation: Animator?) {

                        }

                    }).playOn(lavScanning)
            }, 5000)

        }
        view.ivBack.setOnClickListener {
            listener?.onFragmentBack()
        }
        ivSelectedImage = view.ivSelectedImage

        view.btnUploadImage.setOnClickListener {
            Dexter.withActivity(activity)
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
                        selectImage(activity!!)
                    }
                })
                .check()
        }
        return view
    }

    private fun selectImage(context: Context) {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(context)
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Take Photo" -> {
                    val contentValues = ContentValues()
                    contentValues.put(MediaStore.Images.Media.TITLE, "newPic")
                    contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Image to text")
                    imageUri = activity?.contentResolver?.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    )
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                    listener?.onImageLoad(intent, CAMERA, imageUri)
                }
                options[item] == "Choose from Gallery" -> {
                    val galleryIntent = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    listener?.onImageLoad(galleryIntent, GALLERY, null)
                }
                options[item] == "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    fun onImageLoaded(uri: Uri?) {
        ivSelectedImage.setImageURI(uri)
        if (uri != null) {
            mlInstruction.transitionToEnd()
            btnPredict.visibility = View.VISIBLE
        } else {
            mlInstruction.transitionToStart()
            btnPredict.visibility = View.GONE
        }
//        onImageLoaded((ivSelectedImage.drawable as BitmapDrawable).bitmap)
    }

    private fun showLoading() {
        aviPredicting.show()
        flLoading.visibility = View.VISIBLE
        lavScanning.playAnimation()
    }

    private fun hideLoading() {
        aviPredicting.hide()
        lavScanning.cancelAnimation()
        flLoading.visibility = View.GONE
    }

    private fun onImageLoaded(bitmap: Bitmap?) {
//        if (bitmap != null) {
//            pbLoading.visibility = View.VISIBLE
//            llResult.visibility = View.INVISIBLE
//            incResult.visibility = View.VISIBLE
//
//            Handler().postDelayed({
//                pbLoading.visibility = View.GONE
//                llResult.visibility = View.VISIBLE
//                predictedType = ImagePredictor.predict(bitmap, assets)
//                predictedType?.let { type -> setPredictedType(type) }
//
//            }, WAITING_TIME)
//
//        } else {
//            Toast.makeText(this@MainActivity, "Failed to load photo", Toast.LENGTH_SHORT).show()
//        }
    }

}
