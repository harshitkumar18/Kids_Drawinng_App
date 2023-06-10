package com.example.drawingapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet): View(context, attrs) {

   private var mDrawPath : CustomPath? = null

    private var mCanvasBitmap: Bitmap? = null

    private var mDrawpaint: Paint? = null
    private var mCanvasPaint: Paint?= null
    private var mbrushsize: Float = 0.toFloat()
    private var color = Color.BLACK

    private var canvas:Canvas?=null
    private val mPaths = ArrayList<CustomPath>()

    private val mundoPaths = ArrayList<CustomPath>()

    init{
        setUpDrawing()
    }
    fun onclickundo(){
        if(mPaths.size >0){
            mundoPaths.add(mPaths.removeAt(mPaths.size-1))
            invalidate()
        }
    }
    private fun setUpDrawing(){
        mDrawpaint = Paint()
        mDrawPath = CustomPath(color, mbrushsize)
        mDrawpaint!!.color= color
        mDrawpaint!!.style= Paint.Style.STROKE
        mDrawpaint!!.strokeJoin = Paint.Join.ROUND
        mDrawpaint!!.strokeCap= Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
//        mbrushsize = 20.toFloat()

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!! )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!,0f,0f,mCanvasPaint)
         for(path in mPaths){
                 mDrawpaint!!.color = path.color
                 mDrawpaint!!.strokeWidth = path.brushThickness
                 canvas.drawPath(path, mDrawpaint!!)
         }

         if(!mDrawPath!!.isEmpty) {
             mDrawpaint!!.color = mDrawPath!!.color
             mDrawpaint!!.strokeWidth = mDrawPath!!.brushThickness
             canvas.drawPath(mDrawPath!!, mDrawpaint!!)
         }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchx = event?.x
        val touchy = event?.y

        when(event?.action){
              MotionEvent.ACTION_DOWN ->{
                  mDrawPath!!.color = color
                  mDrawPath!!.brushThickness = mbrushsize

                  mDrawPath!!.reset()

                  if (touchx != null) {
                      if (touchy != null) {
                           mDrawPath!!.moveTo(touchx,touchy)
                      }
                  }
              }
            MotionEvent.ACTION_MOVE ->{
                if (touchx != null) {
                    if (touchy != null) {
                        mDrawPath!!.lineTo(touchx,touchy)
                    }
                }
            }
            MotionEvent.ACTION_UP ->{
                mPaths.add(mDrawPath!!)
                mDrawPath = CustomPath(color,mbrushsize)
            }
            else ->return false
        }
        invalidate()
        return true
    }

    fun setSizeforbrush(newsize : Float){
        mbrushsize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
             newsize, resources.displayMetrics
             )
        mDrawpaint!!.strokeWidth = mbrushsize
    }

    fun setcolor(newcolor: String){
        color = Color.parseColor(newcolor)
        mDrawpaint!!.color = color
    }
    internal inner class CustomPath(var color: Int, var brushThickness: Float): Path() {

    }


}