package com.solt.thiochat.ui.utils

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.View

//We want to use this class from Stack OverFlow to blur images
//https://stackoverflow.com/questions/6795483/create-blurry-transparent-background-effect
class BlurBuilder {
    companion object{
        private const val BITMAP_SCALE = 0.4f
        private const val BLUR_RADIUS = 7.5f

        fun blur(context: Context, bitmap: Bitmap ):Bitmap{
            val width = Math.round(bitmap.width* BITMAP_SCALE)
            val height = Math.round(bitmap.height* BITMAP_SCALE)
            val inputBitmap = Bitmap.createScaledBitmap(bitmap,width,height,false)
            val outputBitmap = Bitmap.createBitmap(inputBitmap)
            //The RenderScript part
            //I know it is deprecated for Android 12 so the option will be for below Android 12
            val renderScript = RenderScript.create(context)
            val blurIntrisic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
            val allocInput = Allocation.createFromBitmap(renderScript,inputBitmap)
            val allocOutput = Allocation.createFromBitmap(renderScript,outputBitmap)

            //Set the blur radius
            blurIntrisic.setRadius(BLUR_RADIUS)
            blurIntrisic.setInput(allocInput)
            blurIntrisic.forEach(allocOutput)
            allocOutput.copyTo(outputBitmap)
            return outputBitmap
        }
        fun getScreenShot(view:View):Bitmap{
            val bitmap = Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            return bitmap

        }
        fun blur(view: View):Bitmap{
        return  blur(view.context,getScreenShot(view))
        }
    }
}