package kz.application.mbti.predictor.fragments

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.fragment_text.*
import kotlinx.android.synthetic.main.fragment_text.view.*
import kotlinx.android.synthetic.main.layout_text_prediction.*
import kotlinx.android.synthetic.main.layout_text_prediction.view.*
import kz.application.mbti.predictor.R
import kz.application.mbti.predictor.listener.OnFragmentActivityListener
import kz.application.mbti.predictor.predictor.TextPredictor

class TextFragment : Fragment() {

    private var listener: OnFragmentActivityListener? = null
    private var changedTextCount: Int = 0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as OnFragmentActivityListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_text, container, false)
        view.btnPredict.setOnClickListener {
            hideSoftKeyboard(activity!!)
            if (isInputTextValid(etInputText.text.toString())) {
                showLoading()
                val predictor =
                    TextPredictor(
                        activity!!.assets,
                        "word_dict.json",
                        view.etInputText.text.toString()
                    )
                predictor.setCallback(object : TextPredictor.DataCallback {
                    override fun onDataProcessed(result: HashMap<String, Int>?) {
                        Handler().postDelayed({
                            YoYo.with(Techniques.ZoomOut).duration(500)
                                .withListener(object : Animator.AnimatorListener {
                                    override fun onAnimationRepeat(animation: Animator?) {

                                    }

                                    override fun onAnimationEnd(animation: Animator?) {
                                        if (result?.keys?.size ?: 0 == 1 ){
                                            val pred = result?.keys.toString()
                                                .substring(1, result?.keys.toString().length - 1)
                                            hideLoading()
                                            listener?.onPredictionResult(pred)
                                        }
                                    }

                                    override fun onAnimationCancel(animation: Animator?) {

                                    }

                                    override fun onAnimationStart(animation: Animator?) {

                                    }

                                }).playOn(view.lavScanning)
                        }, 5000)
                    }
                })
                predictor.loadData()
            }

        }
        view.ivBack.setOnClickListener {
            listener?.onFragmentBack()
        }
        return view
    }

    private fun showLoading() {
        aviPredicting.show()
        flLoadingImage.visibility = View.VISIBLE
        lavScanning.playAnimation()
    }

    private fun hideLoading() {
        lavScanning.cancelAnimation()
        flLoadingImage.visibility = View.GONE
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

    private fun isInputTextValid(text: String): Boolean {
        if (text.isEmpty()) {
            Toasty.error(activity!!, getString(R.string.error_empty_text), Toast.LENGTH_SHORT)
                .show()
            YoYo.with(Techniques.Shake)
                .duration(300)
                .playOn(etInputText)
            return false
        } else {
            if (text.length < 100) {
                Toasty.error(activity!!, getString(R.string.error_min_10), Toast.LENGTH_SHORT)
                    .show()
                YoYo.with(Techniques.Shake)
                    .duration(300)
                    .playOn(etInputText)
                return false
            }
        }
        return true
    }
}
