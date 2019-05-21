package com.rr.scannerview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.animation.LinearInterpolator

/**
 * Created by Rahul Rawat on 4/7/19.
 */

class RScannerOverlayView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    interface RectangleChanged {
        fun onChange(rectangle: RectF?)
    }

    var mRectangleChanged: RectangleChanged? = null
        set(value) {
            field = value
            value?.onChange(scanningRectangle)
        }

    var scanningRectangle: RectF? = null

    // Styleable attributes:
    private var mFrameWidthPercentage: Float = 0.toFloat()
    private var mFrameHeightPercentage: Float = 0.toFloat()
    private var mFrameAspectRatio: Float = 0.toFloat()
    private var mMovingLineWidth: Float = 0.toFloat()
    private var mFrameLineWidth: Float = 0.toFloat()
    private var mOrthogonalFrameLineLength: Float = 0.toFloat()
    private var mPaddingColor: Int = 0
    private var mMovingLineColor: Int = 0
    private var mFrameColor: Int = 0
    private var mUpAndDownSeconds: Float = 0.toFloat()

    // Objects for drawing
    private var mFramePaint: Paint? = null
    private var mMovingLinePaint: Paint? = null
    private var mPaddingPaint: Paint? = null

    // Calculated properties depending on the size and state of the view
    private var mFrameWidth: Int = 0
    private var mFrameHeight: Int = 0
    private var mPaddingWidth: Int = 0
    private var mPaddingHeight: Int = 0
    private var mMovingLineProgress: Float = 0.toFloat()
    private var mLineMovingDown = true
    private var mAnimated = true

    init {

        // Get the attributes with styles applied and resource references resolved
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.RScannerOverlayView,
            0, 0
        )

        // Calculate the pixel-lengths of default values
        val r = context.resources
        val defaultMovingLineWidth = TypedValue
            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, r.displayMetrics)
        val defaultFrameLineWidth = TypedValue
            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, r.displayMetrics)
        val defaultOrthogonalFrameLineLength = TypedValue
            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48f, r.displayMetrics)

        try {
            // Parse the attributes
            mFrameWidthPercentage = a.getFloat(
                R.styleable.RScannerOverlayView_frameWidthPercentage,
                0.75f
            )
            mFrameHeightPercentage = a.getFloat(
                R.styleable.RScannerOverlayView_frameHeightPercentage,
                0.75f
            )
            mFrameAspectRatio = a.getFloat(
                R.styleable.RScannerOverlayView_frameAspectRatio,
                1f
            )

            mMovingLineWidth = a.getDimension(
                R.styleable.RScannerOverlayView_movingLineWidth,
                defaultMovingLineWidth
            )
            mFrameLineWidth = a.getDimension(
                R.styleable.RScannerOverlayView_frameLineWidth,
                defaultFrameLineWidth
            )
            mOrthogonalFrameLineLength = a.getDimension(
                R.styleable.RScannerOverlayView_orthogonalFrameLineLength,
                defaultOrthogonalFrameLineLength
            )

            mFrameColor = a.getColor(
                R.styleable.RScannerOverlayView_frameColor,
                Color.argb(150, 200, 200, 200)
            )
            mMovingLineColor = a.getColor(
                R.styleable.RScannerOverlayView_movingLineColor,
                Color.argb(150, 0, 255, 0)
            )
            mPaddingColor = a.getColor(
                R.styleable.RScannerOverlayView_paddingColor,
                Color.argb(100, 150, 150, 150)
            )
            mUpAndDownSeconds = a.getFloat(R.styleable.RScannerOverlayView_upAndDownSeconds, 3.5f)

            // Init the paints for drawing
            initPaints()
            startMovingLineAnimation()
        } finally {
            a.recycle()
        }
    }

    private fun initPaints() {
        mFramePaint = Paint(0)
        mFramePaint!!.color = mFrameColor
        mFramePaint!!.style = Paint.Style.STROKE
        mFramePaint!!.strokeWidth = mFrameLineWidth

        mMovingLinePaint = Paint(0)
        mMovingLinePaint!!.color = mMovingLineColor
        mMovingLinePaint!!.style = Paint.Style.STROKE
        mMovingLinePaint!!.strokeWidth = mMovingLineWidth

        mPaddingPaint = Paint(0)
        mPaddingPaint!!.color = mPaddingColor
        mPaddingPaint!!.style = Paint.Style.FILL
    }

    private fun startMovingLineAnimation() {
        // Set up the animation of the moving line
        val animation = ValueAnimator.ofFloat(0F, 1F)
        animation.interpolator = LinearInterpolator()
        animation.duration = (mUpAndDownSeconds / 2 * 1000).toLong()
        animation.addUpdateListener { valueAnimator ->
            mMovingLineProgress = valueAnimator.animatedValue as Float
            invalidate()
        }

        // Reverse the animation when it's finished
        animation.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mLineMovingDown = !mLineMovingDown
                startMovingLineAnimation()
            }
        })

        animation.start()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)


        /*Log.d("dexter","$width $height")

        if (isLandscapeMode()) {
            mFrameWidthPercentage = 0.40f
            mFrameHeightPercentage = 0.40f
        }

        // Calculate all size-dependent attributes
        mFrameWidth = (w * mFrameWidthPercentage).toInt()

        if (mFrameAspectRatio > 0) {
            // Ignore the frame height percentage and just use the aspect ratio
            mFrameHeight = (mFrameWidth / mFrameAspectRatio).toInt()
        } else {
            // Use the frame height percentage attribute
            mFrameHeight = (h * mFrameHeightPercentage).toInt()
        }*/

        val smallerDimen = if (w > h) h else w

        mFrameHeight = (smallerDimen * 0.75).toInt()
        mFrameWidth = (smallerDimen * 0.75).toInt()

        mPaddingWidth = (w - mFrameWidth) / 2
        mPaddingHeight = (h - mFrameHeight) / 2

        scanningRectangle = RectF(
            mPaddingWidth.toFloat(),
            mPaddingHeight.toFloat(),
            (mPaddingWidth + mFrameWidth).toFloat(),
            (mPaddingHeight + mFrameHeight).toFloat()
        )
        mRectangleChanged?.onChange(scanningRectangle!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Top Padding
        canvas.drawRect(0f, 0f, this.width.toFloat(), mPaddingHeight.toFloat(), mPaddingPaint!!)
        // Bottom Padding
        canvas.drawRect(
            0f, (mPaddingHeight + mFrameHeight).toFloat(), this.width.toFloat(),
            this.height.toFloat(), mPaddingPaint!!
        )
        // Left Padding
        canvas.drawRect(
            0f, mPaddingHeight.toFloat(), mPaddingWidth.toFloat(), (mPaddingHeight + mFrameHeight).toFloat(),
            mPaddingPaint!!
        )
        // Right Padding
        canvas.drawRect(
            (mPaddingWidth + mFrameWidth).toFloat(), mPaddingHeight.toFloat(), this.width.toFloat(),
            (mPaddingHeight + mFrameHeight).toFloat(), mPaddingPaint!!
        )

        // Draw the frame
        canvas.drawRect(
            mPaddingWidth.toFloat(), mPaddingHeight.toFloat(), (mPaddingWidth + mFrameWidth).toFloat(),
            (mPaddingHeight + mFrameHeight).toFloat(), mFramePaint!!
        )

        // Draw the top orthogonal frame line
        val centerX = (this.width / 2).toFloat()
        val centerY = (this.height / 2).toFloat()
        val halfLineLength = 0.5f * mOrthogonalFrameLineLength
        canvas.drawLine(
            centerX, mPaddingHeight - halfLineLength, centerX,
            mPaddingHeight + halfLineLength, mFramePaint!!
        )
        // Draw the bottom orthogonal frame line
        canvas.drawLine(
            centerX, mPaddingHeight + mFrameHeight - halfLineLength, centerX,
            mPaddingHeight.toFloat() + mFrameHeight.toFloat() + halfLineLength, mFramePaint!!
        )

        // Draw the left orthogonal frame line
        canvas.drawLine(
            mPaddingWidth - halfLineLength, centerY,
            mPaddingWidth + halfLineLength, centerY, mFramePaint!!
        )
        // Draw the right orthogonal frame line
        canvas.drawLine(
            mPaddingWidth + mFrameWidth - halfLineLength, centerY,
            mPaddingWidth.toFloat() + mFrameWidth.toFloat() + halfLineLength, centerY, mFramePaint!!
        )

        // Draw the moving line
        if (mAnimated) {
            val lineAreaHeight = mFrameHeight - mFrameLineWidth
            var relativeY = mMovingLineProgress * lineAreaHeight
            if (!mLineMovingDown) {
                relativeY = lineAreaHeight - relativeY
            }
            val lineY = mPaddingHeight.toFloat() + mFrameLineWidth + relativeY
            canvas.drawLine(
                mPaddingWidth + mFrameLineWidth / 2, lineY,
                mPaddingWidth + mFrameWidth - mFrameLineWidth / 2, lineY, mMovingLinePaint!!
            )
        }
    }

    fun retrieveWH() = Pair(mFrameWidth, mFrameHeight)

    fun retrieveScreenLocation() = intArrayOf(0, 0).apply {
        getLocationOnScreen(this)
    }

    fun retrieveRect(frameWidth: Int, frameHeight: Int) = RectF().apply {

        val screenLocation = retrieveScreenLocation()

        val widthScaleFactor = frameWidth.toFloat() / width.toFloat()
        val heightScaleFactor = frameHeight.toFloat() / height.toFloat()

        left = (mPaddingWidth + screenLocation[Companion.x]) * widthScaleFactor
        top = (mPaddingHeight + screenLocation[Companion.y]) * heightScaleFactor
        right = ((mPaddingWidth + mFrameWidth) + screenLocation[Companion.x]) * widthScaleFactor
        bottom = ((mPaddingHeight + mFrameHeight) + screenLocation[Companion.y]) * heightScaleFactor
    }

    fun setAnimated(animated: Boolean) {
        this.mAnimated = animated
    }

    private fun isLandscapeMode() =
        when (context?.resources?.configuration?.orientation ?: Configuration.ORIENTATION_PORTRAIT) {
            Configuration.ORIENTATION_LANDSCAPE -> true
            Configuration.ORIENTATION_PORTRAIT -> false
            else -> false
        }

    companion object {
        const val x = 0
        const val y = 1
    }
}
