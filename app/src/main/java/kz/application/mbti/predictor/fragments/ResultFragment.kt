package kz.application.mbti.predictor.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_result.view.*
import kz.application.mbti.predictor.Config
import kz.application.mbti.predictor.R

/**
 * A simple [Fragment] subclass.
 */
class ResultFragment : Fragment() {

    companion object {
        const val MBTI_TYPE = "MBTI_TYPE"
        fun newInstance(mbtiType: String): ResultFragment {
            return ResultFragment().apply {
                arguments = Bundle().apply {
                    putString(MBTI_TYPE, mbtiType)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_result, container, false)
        val mbtiType = arguments?.getString(MBTI_TYPE)
        view.avLOILoading.show()
        view.tvType.text = mbtiType
        val specialType = Config.MBTI_TYPE_SPECIALS[Config.MBTI_TYPES.indexOf(mbtiType)]
        view.tvTypeSpecial.text = specialType
        val webSettings = view.webView.settings
        webSettings.javaScriptEnabled = true
        view.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view2: WebView?, url: String?) {
                view.webView.loadUrl(
                    "javascript:(function() {\n" +
                            " document.getElementsByClassName('navbar')[0].style.display='none';\n" +
                            "document.getElementsByClassName('social-cta comp')[0].style.display='none';\n" +
                            "document.getElementsByClassName('comments')[0].style.display='none';\n" +
                            "document.getElementsByClassName('type-bottom')[0].style.display='none'; \n" +
                            "document.getElementsByClassName('navigation-buttons comp')[0].style.display='none'; \n" +
                            "document.getElementsByClassName('footer')[0].style.display='none'; \n" +
                            "})()"
                )
                view.avLOILoading.hide()
                view.avLOILoading.visibility = View.GONE
                view.webView.visibility = View.VISIBLE
            }
        }


        mbtiType?.let {
            view.webView.loadUrl("https://www.16personalities.com/${it.toLowerCase()}-personality")
        }
        return view
    }
}
