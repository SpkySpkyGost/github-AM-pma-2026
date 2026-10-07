package com.example.myapp001bdicethrowcompose

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RawRes

/**
 * Loads an animated image (GIF / animated WebP) from res/raw.
 * Android 9+ (API 28) decodes it as an AnimatedImageDrawable that loops forever;
 * older versions only show the first frame as a still image.
 * Call start() / stop() on the returned drawable (if it is Animatable) to play / pause it.
 */
fun loadAnimatedImage(context: Context, @RawRes resId: Int): Drawable {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.resources, resId)
        ImageDecoder.decodeDrawable(source).also { drawable ->
            if (drawable is AnimatedImageDrawable) {
                drawable.repeatCount = AnimatedImageDrawable.REPEAT_INFINITE
            }
        }
    } else {
        context.resources.openRawResource(resId).use { stream ->
            BitmapDrawable(context.resources, BitmapFactory.decodeStream(stream))
        }
    }
}
