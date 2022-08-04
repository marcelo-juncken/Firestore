package com.cdj.firestore.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import com.cdj.firestore.R
import java.io.IOException

@GlideModule
class GlideLoader(val context: Context) : AppGlideModule() {

    fun loadUserPicture(image: Any, imageView: ImageView, callback: ((isSuccess: Boolean) -> Unit)?) {
        try {
            Glide
                .with(context)
                .load(image)
                .centerCrop() // Scale type of the image.
                .placeholder(R.drawable.ic_user_placeholder) // A default place holder if image is failed to load.
                .into(imageView) // the view in which the image will be loaded.

            if (callback != null) return callback.invoke(true)
        } catch (e: IOException) {
            e.printStackTrace()
            if (callback != null) return callback.invoke(false)
        }
    }

    fun loadProductPicture(image: Any, imageView: ImageView, callback: ((isSuccess: Boolean) -> Unit)?) {
        try {
            Glide
                .with(context)
                .load(image)
                .centerCrop() // Scale type of the image.
                .into(imageView) // the view in which the image will be loaded.

            if (callback != null) return callback.invoke(true)
        } catch (e: IOException) {
            e.printStackTrace()
            if (callback != null) return callback.invoke(false)
        }
    }

}
