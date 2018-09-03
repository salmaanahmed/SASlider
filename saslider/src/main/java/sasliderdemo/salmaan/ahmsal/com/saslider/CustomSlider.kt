package sasliderdemo.salmaan.ahmsal.com.saslider

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.wisemani.customview.CircleView
import com.wisemani.patienttouch.customview.TriangleShapeView
import kotlinx.android.synthetic.main.custom_slider.view.*
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.roundToInt

/**
 * Created by salmaanahmed on 20/07/2018.
 */
class CustomSlider @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), ViewTreeObserver.OnScrollChangedListener {

    //// Private variables
    private var circleWidth: Int
    private var bigCircleWidth: Int
    private var thumbWidth: Int
    private var spaceSize: Int
    private var textViewSize: Int
    private var textViewText: Double = 0.0
    private var numberOfTextViews = 0
    private var spaceBetweenCircles: Int
    private var isKeyboardShowing = false
    private var onLayoutCalled = false  //Init view only once after layout changed

    //// Public variables
    var min: Double = 0.0   //Set,Get minimum value of slider, do it before slider is added to any layout
        set(value) {
            if (lowerThreshold == field) lowerThreshold = value
            field = value
        }

    var max: Double = 100.0 //Set,Get maximum value of slider, do it before slider is added to any layout
        set(value) {
            if (upperThreshold == field) upperThreshold = value
            field = value
        }

    var default: Double = 50.0 //Set,Get maximum value of slider, do it before slider is added to any layout
    var upperThreshold: Double = max
    var lowerThreshold: Double = min
    var criticalColor = Color.RED
    var sliderColor = Color.BLUE //Set,Get color of slider, do it before slider is added to any layout
    var circleColor = Color.GRAY //Set,Get circle and text color of slider, do it before slider is added to any layout
    var editTextColor = Color.GRAY //Set,Get circle and text color of slider, do it before slider is added to any layout

    // Set,Get selected index of slider
    // Setting any value may scroll the slider
    // DONOT SET VALUE BEFORE SLIDER IS DISPLAYED, ADDED TO ANY LAYOUT
    var selectedIndex: Double = 0.0
        set(value) {
            if (onLayoutCalled) {
                field = value
                scrollView.smoothScrollTo(getPixelFromNumber(value), 0) // Scroll to the set value
            }
        }

    // Get status of value from slider
    // Setting critical value changes the color of thumb
    var isCritical = false
        set(value) {
            if (field == value) return
            field = value
            if (isCritical) animateColorChange(sliderColor, criticalColor)
            else animateColorChange(criticalColor, sliderColor)
        }

    // It will convert the slider to type decimal
    var isDecimal: Boolean = false
        set(value) {
            field = value
            if (value) editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL // If decimal type slider, show decimals in keyboard
            else editText.inputType = InputType.TYPE_CLASS_NUMBER   // If int type slider, only show int values
        }

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_slider, this, true)

            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomSlider, 0, 0)
            sliderColor = typedArray.getColor(R.styleable.CustomSlider_sliderColor, Color.BLUE) // Set value from XML attributes or set default slider color
            circleColor = typedArray.getColor(R.styleable.CustomSlider_circleColor, Color.GRAY) // Set value from XML attributes or set default circle/text color
            criticalColor = typedArray.getColor(R.styleable.CustomSlider_criticalColor, Color.RED) // Set value from XML attributes or set default circle/text color
            editTextColor = typedArray.getColor(R.styleable.CustomSlider_editTextBorderColor, Color.LTGRAY) // Set value from XML attributes or set default edit text color
            circleWidth = typedArray.getDimensionPixelSize(R.styleable.CustomSlider_circleSize, resources.getDimension(R.dimen.circleSize).toInt()) // Set value from XML attributes or set default size of circle
            bigCircleWidth = circleWidth * 2    // Big circle is twice the size of small circle
            thumbWidth = (bigCircleWidth * 3)   // Thumb should be large enough to be seen
            spaceSize = bigCircleWidth * 2      // Space is twice the size of big circle
            textViewSize = (spaceSize * 5) + (bigCircleWidth * 5)   // Text View is size of 5 circles and 5 spaces as text is displayed at every 5th circle
            spaceBetweenCircles = bigCircleWidth + spaceSize        // Space between center point is equal to two half big circles i.e. one big circle and size of space
            isDecimal = typedArray.getBoolean(R.styleable.CustomSlider_isDecimal, false) // Set value from XML attributes or set default slider type
            min = typedArray.getFloat(R.styleable.CustomSlider_minValue, 1.0f).toDouble().roundToOneDecimal()  // Set value from XML attributes or set default min value
            max = typedArray.getFloat(R.styleable.CustomSlider_maxValue, 10.0f).toDouble().roundToOneDecimal()  // Set value from XML attributes or set default max value
            upperThreshold = typedArray.getFloat(R.styleable.CustomSlider_upperThreshold, max.toFloat()).toDouble().roundToOneDecimal()  // Set value from XML attributes or set default threshold value
            lowerThreshold = typedArray.getFloat(R.styleable.CustomSlider_lowerThreshold, min.toFloat()).toDouble().roundToOneDecimal()  // Set value from XML attributes or set default threshold value
            default = typedArray.getFloat(R.styleable.CustomSlider_defaultValue, min.toFloat()).toDouble().roundToOneDecimal()  // Set value from XML attributes or set default value
    }


    /**
     * Add init view when layout changed
     */
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (!onLayoutCalled) {
            addSpaces()
            initView()
            onLayoutCalled = true
        }
    }

    /**
     * Adds spaces to both ends of the slider
     */
    private fun addSpaces() {
            val viewFirst = View(context)
            viewFirst.layoutParams = FrameLayout.LayoutParams(this.scrollView.width / 2 - bigCircleWidth / 2, 1)

            val viewLast = View(context)
            viewLast.layoutParams = FrameLayout.LayoutParams(this.scrollView.width / 2 - bigCircleWidth / 2, 1)

            circleLayout.addView(viewFirst, 0)
            circleLayout.addView(viewLast)
    }

    /**
     * Populate text views, add spaces, create circles, customize edit text, set colors, set event listeners.
     */
    private fun initView() {
        addSpaceToTextViewLayout()

        // Set initial value of text view on bottom of the slider, must be multiple of 5
        if (isDecimal) {
            textViewText = (min * 10 + (5 - (min * 10 % 5)))
            if ((min * 10 % 5) <= 0.0) textViewText -= 5
            textViewText /= 10
        } else {
            textViewText = (min + (5 - (min % 5)))
            if ((min % 5) <= 0.0) textViewText -= 5
        }


        scrollView.isHorizontalScrollBarEnabled = false
        setScrollListener()


        addCircles()
        addTextViews()

        // Colored circle marker to show the selected index/value
        lineLayout.layoutParams.height = thumbWidth
        circleSelection.layoutParams.height = thumbWidth
        circleSelection.layoutParams.width = thumbWidth
        circleSelection.background.setColorFilter(sliderColor,  PorterDuff.Mode.SRC_IN)

        // Customised edit text to show the selected index/value
        customizeEditText()

        scrollViewChild.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                selectedIndex = getNumberFromPixel(event.x.toInt() - this.scrollView.width / 2)
            } else if (event.action == MotionEvent.ACTION_DOWN) {
                hideSoftKeyboard(context, editText)
            }
            return@setOnTouchListener true
        }

        editText.setTextProgrammatically(if (isDecimal) default.toString() else default.toInt().toString()) // set slider value
        Handler().postDelayed({
            this.selectedIndex = default
        }, 500L)
    }

    /**
     * Add space before the textview to align it with the dots
     */
    private fun addSpaceToTextViewLayout() {
        val firstSpace = this.scrollView.width / 2
        val mod = if (isDecimal) (min * 10) % 5 else min % 5
        if (mod == 0.toDouble()) {
            numberLayout.addView(getSpace(firstSpace - (textViewSize / 2)), 0)
        } else {
            numberLayout.addView(getSpace(firstSpace + ((textViewSize) * (((2.5 - mod) / 5))).toInt()), 0)
        }
    }

    /**
     * Add circles to the view
     * Set circle color
     * Enlarge every 5th circle
     */
    private fun addCircles() {
        // Create new linear layout and add it to circleLayout

        val maxLimit: Int
        val minLimit: Int

        // Change min max limit if slider type is decimal
        if (isDecimal) {
            maxLimit = (max * 10).toInt()
            minLimit = (min * 10).toInt()
        } else {
            maxLimit = max.toInt()
            minLimit = min.toInt()
        }

        val linearLayout = LinearLayout(context)
        linearLayout.orientation = HORIZONTAL

        for (i in minLimit..maxLimit) {
            if (i % 5 == 0) { // Add big circle on every 5th index
                linearLayout.addView(getCircle(bigCircleWidth, i))
                linearLayout.addView(getSpace(spaceSize))
                numberOfTextViews++
            } else {
                linearLayout.addView(getCircle(circleWidth, i))
                linearLayout.addView(getSpace(spaceSize))
            }
        }
        lineBackground.setBackgroundColor(circleColor)
        linearLayout.removeViewAt(linearLayout.childCount - 1)
        circleLayout.addView(linearLayout, 1)
    }

    /**
     * Create rounded corners add triangle and set color to edit text
     * Set listeners to edit text
     * Restrict user to copy-paste
     */
    private fun customizeEditText() {
        val shape = GradientDrawable()
        shape.setColor(Color.WHITE)
        editText.background = shape
        editText.setPadding(10, 0, 10, 0)
        editText.gravity = Gravity.CENTER


        frameEditText.setBackgroundColor(editTextColor)

        var triangle = TriangleShapeView(context, editTextColor)
        trLayout.addView(triangle)
        triangle.layoutParams.width = (bigCircleWidth * 1.25).toInt()
        triangle.layoutParams.height = (bigCircleWidth * 1.25).toInt()

        editText.disableCopyPaste()
        setEditTextListener()
    }


    /**
     * Edit text listener
     * If edit text is empty, scroll to start
     * Do nothing if value is less than min
     * Do not let user enter grater value than max
     * Show cursor only if keyboard is pulled up
     */
    private fun setEditTextListener() {
        editText.onTextChangedManually { _, position ->
            if (editText.text.isEmpty()) {
                scrollView.smoothScrollTo(0, 0)
            } else if (editText.text.toString() == "." || editText.text.toString().toDouble() < min) {
                return@onTextChangedManually
            } else if (editText.text.toString().toDouble() > max) {
                editText.removeCharacterAt(position)
            } else {
                selectedIndex = editText.text.toString().toDouble()
            }
        }

        editText.setSelectAllOnFocus(true)

        // Show cursor only if keyboard is pulled up
        editText.viewTreeObserver.addOnGlobalLayoutListener({
            if (keyboardShown(editText.rootView)) {
                isKeyboardShowing = true
                editText.isCursorVisible = true
            } else {
                if (isKeyboardShowing) {
                    isKeyboardShowing = false
                    editText.isCursorVisible = false
                    updateText()
                    editText.setSelection(editText.text.count())
                }
            }
        })
    }

    /**
     * Update text to the edit text
     */
    private fun updateText() {
        if (isDecimal) {
            editText.setTextProgrammatically(selectedIndex.toString())
        } else {
            editText.setTextProgrammatically(selectedIndex.toInt().toString())
        }
    }

    /**
     * If keyboard is showing or not
     */
    private fun keyboardShown(rootView: View): Boolean {
        val softKeyboardHeight = 100
        val r = Rect()
        rootView.getWindowVisibleDisplayFrame(r)
        val dm = rootView.resources.displayMetrics
        val heightDiff = rootView.bottom - r.bottom
        return heightDiff > softKeyboardHeight * dm.density
    }

    /**
     * Create circle with the provided size, index and selected color
     */
    private fun getCircle(size: Int, index: Int): View {
        val image = CircleView(context, size / 2, circleColor)
        image.layoutParams = LinearLayout.LayoutParams(bigCircleWidth, thumbWidth)
        image.tag = index
        return image
    }

    /**
     * Returns space view
     */
    private fun getSpace(spaceWidth: Int): View {
        val space = View(context)
        space.layoutParams = LinearLayout.LayoutParams(spaceWidth, 1)
        return space
    }

    /**
     * Add all the textviews in the laout
     */
    private fun addTextViews() {
        for (i in 1..numberOfTextViews) {
            numberLayout.addView(getNumberTextView())
        }
    }

    /**
     * Setup scroll listener
     */
    private fun setScrollListener() {
        scrollView.viewTreeObserver.addOnScrollChangedListener(this@CustomSlider)
    }

    /**
     * Scroll changed listener
     * Update edit text
     */
    override fun onScrollChanged() {
        var value = getNumberFromPixel(scrollView.scrollX)
        if (isDecimal) editText.setTextProgrammatically((value).toString())
        else editText.setTextProgrammatically((value).roundToInt().toString())
        editText.setSelection(editText.text.count())

        /// scroll start and end logic
        if (lastScrollUpdate == -1L) {
            onScrollStart()
            postDelayed(ScrollStateHandler(), 100)
        }

        lastScrollUpdate = System.currentTimeMillis()


        Thread().run {
            isCritical = value > upperThreshold || value < lowerThreshold
        }
//        runOnUiThread {
//            isCritical = value > upperThreshold || value < lowerThreshold
//        }
    }

    /**
     * Get text view
     */
    private fun getNumberTextView(): View {
        val textView = TextView(context)
        textView.setTextColor(circleColor)
        textView.layoutParams = LinearLayout.LayoutParams(textViewSize, LinearLayout.LayoutParams.WRAP_CONTENT)
        textView.gravity = Gravity.CENTER
        if (isDecimal) {
            textView.text = textViewText.toString()
            textViewText += 0.5
        } else {
            textView.text = textViewText.toInt().toString()
            textViewText += 5
        }
        return textView
    }

    private var lastScrollUpdate: Long = -1

    /**
     * Handles scroll state
     */
    private inner class ScrollStateHandler : Runnable {
        override fun run() {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastScrollUpdate > 100) {
                lastScrollUpdate = -1
                onScrollEnd()
            } else {
                postDelayed(this, 100)
            }
        }
    }

    private fun onScrollStart() {
        Log.d("ScrollView", "Scroll Started")
    }

    /**
     * Animate and animate slider to the nearest mark when scrolling ends
     */
    fun onScrollEnd() {
        Log.d("ScrollView", "Scroll Ended")
        selectedIndex = getNumberFromPixel(scrollView.scrollX)

    }

    /**
     * Returns selected value from the pixel of slider
     */
    private fun getNumberFromPixel(pixel: Int): Double {
        return if (isDecimal) {
            val numberFromPixel = ((((pixel) / spaceBetweenCircles.toDouble()).roundToInt()) + (min * 10))
            numberFromPixel / 10
        } else {
            Math.round(((pixel) / spaceBetweenCircles.toDouble())) + min
        }
    }

    /**
     * Returns pixel of slider from the value passed
     */
    private fun getPixelFromNumber(number: Double): Int {
        if (isDecimal) {
            return ((number - min) * spaceBetweenCircles * 10).toInt()
        }
        return (((number - min) * spaceBetweenCircles)).toInt()
    }

    /**
     * Hides the soft keyboard
     */
    private fun hideSoftKeyboard(context: Context, field: EditText) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(field.windowToken, 0)
    }

    /**
     * Animate color change when slider moves to critical value
     */
    private fun animateColorChange(from: Int, to: Int) {
        val anim = ValueAnimator.ofArgb(from, to)
        anim.addUpdateListener { valueAnimator ->
            circleSelection.background.setColorFilter(valueAnimator.animatedValue as Int,  PorterDuff.Mode.SRC_IN)
        }
        anim.duration = 500
        anim.start()
    }

}


///// Extension Functions
/**
 * Set text programmatically
 * Text watcher does not fires when text is set using this method
 */
private fun EditText.setTextProgrammatically(string: String) {
    this.tag = "SomeShit"
    this.setText(string)
    this.tag = null
}

/**
 * Removes character from edit text at any index passed
 */
private fun EditText.removeCharacterAt(position: Int) {
    val text = editText.text.toString()
    editText.setTextProgrammatically(text.removeRange(position, position + 1))
    editText.setSelection(editText.text.count())
}

/**
 * Listens to change in edit text
 * Only if value is changed by user himself
 * Does not fires when text changed programmatically
 */
private fun EditText.onTextChangedManually(onTextChangedManually: (String, Int) -> Unit) {
    val editTextThis = this
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, start: Int, p2: Int, p3: Int) {
            if (editTextThis.tag == null) {
                onTextChangedManually.invoke(p0.toString(), start)
            }
        }

        override fun afterTextChanged(editable: Editable?) {
        }
    })
}

/**
 * Disables copy-paste actions
 */
private fun TextView.disableCopyPaste() {
    customSelectionActionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu): Boolean {
            return false
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {}
    }
    isLongClickable = false
    setTextIsSelectable(false)
}

/**
 * Rounds to one decimal place
 */
private fun Double.roundToOneDecimal(): Double {
    val df = DecimalFormat("#.#")
    df.roundingMode = RoundingMode.CEILING
    return df.format(this).toDouble()
}