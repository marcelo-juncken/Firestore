package com.cdj.firestore.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cdj.firestore.R
import com.cdj.firestore.firestore.FirestoreClass
import com.cdj.firestore.models.Product
import com.cdj.firestore.utils.Constants
import com.cdj.firestore.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_add_product.*
import java.io.IOException

class AddProductActivity : BaseActivity() {

    private var mSelectedImageFileUri: Uri? = null

    private var mProductImageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        System.out.println(1)
        setContentView(R.layout.activity_add_product)

        setupActionBar()
        System.out.println(2)
        configClicks()
    }

    private fun configClicks() {
        iv_add_update_product.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this@AddProductActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(this@AddProductActivity)
            } else {
                ActivityCompat.requestPermissions(
                    this@AddProductActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        btn_submit.setOnClickListener { uploadProductImage() }
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_add_product_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_add_product_activity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun validateProductDetails(): Boolean {
        return when {
            mSelectedImageFileUri == null -> {
                showErrorSnackBar(getString(R.string.err_msg_select_product_image), true)
                false
            }
            et_product_title.text.toString().trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_product_title), true)
                false
            }
            et_product_price.text.toString().trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_product_price), true)
                false
            }
            et_product_description.text.toString().trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_product_description), true)
                false
            }
            et_product_quantity.text.toString().trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_product_quantity), true)
                false
            }
            else -> {
                true
            }
        }
    }

    private fun uploadProductImage() {
        if (validateProductDetails()) {
            showProgressDialog(null)

            FirestoreClass().uploadImageToCloudStorage(
                this@AddProductActivity,
                mSelectedImageFileUri,
                Constants.PRODUCT_IMAGE
            )


        }
    }

    fun imageUploadSuccess(imageURL: String) {
        mProductImageUrl = imageURL
        updateProductDetails()
    }

    private fun updateProductDetails() {
        val username =
            getSharedPreferences(Constants.MYSHOPAPP_PREFERENCES, Context.MODE_PRIVATE).getString(
                Constants.LOGGED_IN_USERNAME,
                ""
            )!!

        val product = Product(
            FirestoreClass().getCurrentUserID(),
            username,
            et_product_title.text.toString().trim(),
            et_product_price.text.toString().trim(),
            et_product_description.text.toString().trim(),
            et_product_quantity.text.toString().trim(),
            mProductImageUrl
        )

        FirestoreClass().uploadProductDetails(this@AddProductActivity, product)
    }

    fun productUploadSuccess() {
        hideProgressDialog()
        showErrorSnackBar(getString(R.string.product_uploaded_success_message), false)

        finish()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.PICK_IMAGE_REQUEST_CODE) {
                if (data != null) {
                    iv_add_update_product.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_vector_edit
                        )
                    )
                    mSelectedImageFileUri = data.data!!
                    try {
                        GlideLoader(this@AddProductActivity).loadUserPicture(
                            mSelectedImageFileUri!!,
                            iv_product_image, null
                        )
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@AddProductActivity,
                            resources.getString(R.string.image_selection_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            // A log is printed when user close or cancel the image selection.
            Log.e("Request Cancelled", "Image selection cancelled")
        }
    }

}