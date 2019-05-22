package com.rr.scannerview

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.rr.scannerview.R


/**
 * Created by Rahul Rawat on 4/7/19.
 */

class RScannerOverlayView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    interface RectangleChanged {
        fun onChange(rectangle: RectF?)
    }

    // Styleable attributes:
    private var mPaddingColor: Int = 0

    // Objects for drawing
    private var mPaddingPaint: Paint? = null
    private var mBorderPaint: Paint? = null

    // Calculated properties depending on the size and state of the view
    private var mFrameWidth: Int = 0
    private var mFrameHeight: Int = 0
    private var mPaddingWidth: Int = 0
    private var mPaddingHeight: Int = 0
    private var mDefaultBorderColor: Int? = null
    private var mDefaultBorderStrokeWidth: Int? = null
    private var mDefaultBorderLineLength: Int? = null
    private val path = Path()

    var mRectangleChanged: RectangleChanged? = null
    var scanningRectangle: RectF? = null

    init {

        // Get the attributes with styles applied and resource references resolved
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.RScannerOverlayView,
            0, 0
        )

        try {
            mPaddingColor = a.getColor(
                R.styleable.RScannerOverlayView_paddingColor,
                Color.argb(80, 0, 0, 0)
            )

            mDefaultBorderColor = a.getColor(
                R.styleable.RScannerOverlayView_frameColor,
                Color.argb(255, 175, 237, 68)
            )

            mDefaultBorderStrokeWidth = resources.getInteger(R.integer.viewfinder_border_width)
            mDefaultBorderLineLength = resources.getInteger(R.integer.viewfinder_border_length)
            // Init the paints for drawing
            initPaints()
        } finally {
            a.recycle()
        }
    }

    private fun initPaints() {
        mPaddingPaint = Paint(0)
        mPaddingPaint!!.color = mPaddingColor
        mPaddingPaint!!.style = Paint.Style.FILL

        mBorderPaint = Paint()
        mBorderPaint!!.color = mDefaultBorderColor!!
        mBorderPaint!!.style = Paint.Style.STROKE
        mBorderPaint!!.strokeWidth = mDefaultBorderStrokeWidth?.toFloat() ?: 2F
        mBorderPaint!!.isAntiAlias = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

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

        val framingRect = scanningRectangle!!
        val borderLength = mDefaultBorderLineLength ?: 2

        path.moveTo(framingRect.left, framingRect.top + borderLength)
        path.lineTo(framingRect.left, framingRect.top)
        path.lineTo(framingRect.left + borderLength, framingRect.top)
        canvas.drawPath(path, mBorderPaint!!)

        // Top-right corner
        path.moveTo(framingRect.right, framingRect.top + borderLength)
        path.lineTo(framingRect.right, framingRect.top)
        path.lineTo(framingRect.right - borderLength, framingRect.top)
        canvas.drawPath(path, mBorderPaint!!)

        // Bottom-right corner
        path.moveTo(framingRect.right, framingRect.bottom - borderLength)
        path.lineTo(framingRect.right, framingRect.bottom)
        path.lineTo(framingRect.right - borderLength, framingRect.bottom)
        canvas.drawPath(path, mBorderPaint!!)

        // Bottom-left corner
        path.moveTo(framingRect.left, framingRect.bottom - borderLength)
        path.lineTo(framingRect.left, framingRect.bottom)
        path.lineTo(framingRect.left + borderLength, framingRect.bottom)
        canvas.drawPath(path, mBorderPaint!!)
    }
}