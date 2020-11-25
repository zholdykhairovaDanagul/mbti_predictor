package kz.application.mbti.predictor

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.util.IOUtils
import kotlinx.android.synthetic.main.activity_splash_screen.*

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        try {
            val inputStream = assets.open("splach_anim_2.gif")
            val bytes = IOUtils.toByteArray(inputStream)
            gifIVSplash.setBytes(bytes)
            gifIVSplash.startAnimation()
        } catch (ex: Exception) {
            ex.localizedMessage
        }

        Handler().postDelayed({
            startActivity(Intent(this, MainPageActivity::class.java))
            finish()
        }, 2500)
    }
}
