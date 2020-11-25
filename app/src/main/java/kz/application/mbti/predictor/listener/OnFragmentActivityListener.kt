package kz.application.mbti.predictor.listener

import android.content.Intent
import android.net.Uri
import java.io.Serializable

interface OnFragmentActivityListener : Serializable {
    fun onFragmentBack()

    fun onImageLoad(intent: Intent, type: Int, imageUri: Uri?)

    fun onPredictionResult(output: String)
}