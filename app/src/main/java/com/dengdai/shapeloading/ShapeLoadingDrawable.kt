package com.dengdai.shapeloading

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.view.animation.DecelerateInterpolator
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.properties.Delegates


class ShapeLoadingDrawable : Drawable(), Animatable {
    private var mPaint: Paint = Paint()
    var mCurrentShape: Shape
    private var mWidth by Delegates.notNull<Float>()
    private var mHeight by Delegates.notNull<Float>()

    var translateY by Delegates.notNull<Float>()

    private var scale = 0f
    private var rotateRect: Float = 0f
    private var rotateTriangle: Float = 0f


    private var upAnimatorSet: AnimatorSet? = null
    private var downAnimatorSet: AnimatorSet? = null

    private val rectLength: Float = 100f
    private val radius: Float = rectLength / 2

    init {

        mPaint.isAntiAlias = true
        mCurrentShape = Shape.SHAPE_CIRCLE
    }

    override fun onBoundsChange(bounds: Rect?) {
        super.onBoundsChange(bounds)
        mWidth = bounds!!.width().toFloat()
        mHeight = bounds.height().toFloat()
    }

    override fun draw(canvas: Canvas) {

        canvas.save()
        drawLoading(canvas)
        canvas.restore()

        canvas.save()
        drawShadow(canvas)
        canvas.restore()

        canvas.save()
        drawText(canvas)
        canvas.restore()
    }

    override fun setAlpha(alpha: Int) {

    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

    @SuppressLint("WrongConstant")
    override fun getOpacity(): Int {
        return PixelFormat.RGBA_8888
    }

    override fun start() {
        startAnimation()
    }

    override fun stop() {
        stopAnimation()
    }

    override fun isRunning(): Boolean {
        return upAnimatorSet!!.isRunning || downAnimatorSet!!.isRunning
    }


    private fun drawLoading(canvas: Canvas) {
        //上抛下落 平移
        canvas.translate(0f, translateY)
        //三角形的旋转中心和正方形有所区别
        if (mCurrentShape === Shape.SHAPE_TRIANGLE) canvas.rotate(
            rotateTriangle,
            mWidth / 2,
            mHeight / 2 - rectLength / 2 + 200 / 3
        ) else canvas.rotate(rotateRect, mWidth / 2, mHeight / 2)
        when (mCurrentShape) {
            Shape.SHAPE_CIRCLE -> drawCircle(canvas)
            Shape.SHAPE_RECT -> drawRect(canvas)
            Shape.SHAPE_TRIANGLE -> drawTriangle(canvas)
        }
    }

    //画圆
    private fun drawCircle(canvas: Canvas) {
        mPaint.color = Color.parseColor("#aa738ffe")
        canvas.drawCircle(mWidth / 2, mHeight / 2, radius, mPaint)
    }

    //画正方形
    private fun drawRect(canvas: Canvas) {
        mPaint.color = Color.parseColor("#aae84e49")
        canvas.drawRect(
            mWidth / 2 - rectLength / 2,
            mHeight / 2 - rectLength / 2,
            mWidth / 2 + rectLength / 2,
            mHeight / 2 + rectLength / 2,
            mPaint
        )
    }

    //画三角形（正三角形）
    private fun drawTriangle(canvas: Canvas) {
        mPaint.color = Color.parseColor("#aa72d572")
        val path = Path()
        path.moveTo(mWidth / 2, mHeight / 2 - rectLength / 2)
        path.lineTo(
            (mWidth / 2 - sqrt(rectLength.toDouble().pow(2.0) / 3)).toFloat(),
            mHeight / 2 + rectLength / 2
        )
        path.lineTo(
            (mWidth / 2 + sqrt(rectLength.toDouble().pow(2.0) / 3)).toFloat(),
            mHeight / 2 + rectLength / 2
        )
        path.close()
        canvas.drawPath(path, mPaint)
    }

    //画阴影部分椭圆
    private fun drawShadow(canvas: Canvas) {
        mPaint.color = Color.parseColor("#25808080")
        //椭圆缩放
        canvas.scale(scale, scale, mWidth / 2, mHeight / 2 + 90)
        canvas.drawArc(
            mWidth / 2 - rectLength / 2,
            mHeight / 2 + 80,
            mWidth / 2 + 50,
            mHeight / 2 + 100,
            0f,
            360f,
            false,
            mPaint
        )
    }

    //写文字
    private fun drawText(canvas: Canvas) {
        mPaint.textSize = 45f
        mPaint.color = Color.DKGRAY
        mPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("玩命加载中…", mWidth / 2, mHeight / 2 + 170, mPaint)
    }

    //形状的枚举
    enum class Shape {
        // 三角
        SHAPE_TRIANGLE,  // 四边形
        SHAPE_RECT,  // 圆形
        SHAPE_CIRCLE
    }

    private fun exchangeDraw() {
        mCurrentShape =
            when (mCurrentShape) {
                Shape.SHAPE_CIRCLE -> Shape.SHAPE_RECT
                Shape.SHAPE_RECT -> Shape.SHAPE_TRIANGLE
                Shape.SHAPE_TRIANGLE -> Shape.SHAPE_CIRCLE
            }
    }

    /**
     * 上抛动画
     */

    fun upAnimation() {
        val upAnimation: ValueAnimator = ValueAnimator.ofFloat(0f, -200f)

        upAnimation.interpolator = DecelerateInterpolator(1.2f)
        upAnimation.addUpdateListener {
            translateY = upAnimation.animatedValue as Float
            invalidateSelf()
        }

        upAnimation.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                downAnimation()
            }
        })

        val scaleAnimation = ValueAnimator.ofFloat(1f, 0.3f)
        scaleAnimation.interpolator = DecelerateInterpolator(1.2f)
        scaleAnimation.addUpdateListener { scale = scaleAnimation.animatedValue as Float }


        val rotateTriangleAnimation = ValueAnimator.ofFloat(0f, 120f)
        rotateTriangleAnimation.interpolator = DecelerateInterpolator(1.2f)
        rotateTriangleAnimation.addUpdateListener {
            rotateTriangle = rotateTriangleAnimation.animatedValue as Float
        }

        val rotateRectAnimation = ValueAnimator.ofFloat(0f, 180f)
        rotateRectAnimation.interpolator = DecelerateInterpolator(1.2f)
        rotateRectAnimation.addUpdateListener {
            rotateRect = rotateRectAnimation.animatedValue as Float
        }

        upAnimatorSet = AnimatorSet()
        upAnimatorSet!!.duration = 300
        upAnimatorSet!!.playTogether(
            upAnimation,
            scaleAnimation,
            rotateTriangleAnimation,
            rotateRectAnimation
        )
        upAnimatorSet!!.start()

    }

    /**
     * 下落动画
     */
    private fun downAnimation() {
        val downAnimation: ValueAnimator = ValueAnimator.ofFloat(-200f, 0f)
        downAnimation.interpolator = DecelerateInterpolator(1.2f)
        downAnimation.addUpdateListener {
            translateY = downAnimation.animatedValue as Float
            invalidateSelf()

        }


        downAnimation.addListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    upAnimation()
                    exchangeDraw()
                }
            })


        val scaleAnimation: ValueAnimator = ValueAnimator.ofFloat(0.3f, 1f)
        scaleAnimation.interpolator = DecelerateInterpolator(1.2f)
        scaleAnimation.addUpdateListener {
            scale = scaleAnimation.animatedValue as Float
        }


        downAnimatorSet = AnimatorSet()
        downAnimatorSet!!.duration = 500
        downAnimatorSet!!.playTogether(downAnimation, scaleAnimation)
        downAnimatorSet!!.start()
    }

    private fun startAnimation() {
        upAnimation()
    }

    private fun stopAnimation() {
        if (upAnimatorSet != null && upAnimatorSet!!.isStarted) {
            upAnimatorSet!!.end()
        }
        if (downAnimatorSet != null && downAnimatorSet!!.isStarted) {
            downAnimatorSet!!.end()
        }
    }

}