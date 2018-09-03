package sasliderdemo.salmaan.ahmsal.com

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import sasliderdemo.salmaan.ahmsal.com.saslider.SASlider

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val slider = SASlider(this@MainActivity)
        slider.sliderColor = Color.BLUE
        slider.criticalColor = Color.BLACK
        slider.min = 0.0
        slider.max = 50.0
        slider.isDecimal = true
        linearLayout.addView(slider)


    }
}
