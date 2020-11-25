package kz.application.mbti.predictor

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_main_page.*
import kz.application.mbti.predictor.fragments.ImageFragment
import kz.application.mbti.predictor.fragments.ResultFragment
import kz.application.mbti.predictor.fragments.TextFragment
import kz.application.mbti.predictor.listener.OnFragmentActivityListener

class MainPageActivity : AppCompatActivity(), OnFragmentActivityListener {

    enum class PredictionType {
        IMAGE,
        TEXT,
        RESULT
    }

    private var current: PredictionType = PredictionType.TEXT
    private var currentFragment: Fragment? = null
    private var imageUri: Uri? = null
    private var predictedPersonalityType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        slRight.setOnTouchListener { v, event ->
            current = PredictionType.IMAGE
            if (event.action == MotionEvent.ACTION_UP)
                onFragmentChanged(PredictionType.IMAGE)
            false
        }

        slLeft.setOnTouchListener { v, event ->
            current = PredictionType.TEXT
            if (event.action == MotionEvent.ACTION_UP)
                onFragmentChanged(PredictionType.TEXT)
            false
        }
    }

    private fun onFragmentChanged(type: PredictionType) {
        currentFragment = when (type) {
            PredictionType.IMAGE -> {
                ImageFragment()
            }
            PredictionType.TEXT -> {
                TextFragment()
            }
            PredictionType.RESULT -> {
                predictedPersonalityType?.let { ResultFragment.newInstance(it) }
            }
        }
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        if (current == PredictionType.RESULT) transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right)
        currentFragment?.let { transaction.replace(R.id.fragmentMain, it) }
        transaction.commit()
    }

    override fun onFragmentBack() {
        when (current) {
            PredictionType.TEXT -> slLeft.performClick()
            PredictionType.IMAGE -> slRight.performClick()
            PredictionType.RESULT -> mlMain.transitionToStart()
        }
    }

    override fun onImageLoad(intent: Intent, type: Int, imageUri: Uri?) {
        this.imageUri = imageUri
        startActivityForResult(intent, type)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_CANCELED) {
            return
        }
        try {
            when (requestCode) {
                kz.application.mbti.predictor.fragments.CAMERA -> {
                    CropImage.activity(imageUri).setGuidelines(CropImageView.Guidelines.ON)
                        .start(this@MainPageActivity)
                }
                kz.application.mbti.predictor.fragments.GALLERY -> {
                    try {
                        CropImage.activity(data?.data).setGuidelines(CropImageView.Guidelines.ON)
                            .start(this@MainPageActivity)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val res = CropImage.getActivityResult(data)
                    if (resultCode == Activity.RESULT_OK) {
                        val resultUri = res.uri
                        if (currentFragment is ImageFragment) {
                            (currentFragment as ImageFragment).onImageLoaded(resultUri)
                        }
                    }
                }
            }
        } catch (ex: java.lang.Exception) {
            ex.localizedMessage
        }
    }

    override fun onPredictionResult(output: String) {
        predictedPersonalityType = output
        onFragmentChanged(PredictionType.RESULT)
    }
}
