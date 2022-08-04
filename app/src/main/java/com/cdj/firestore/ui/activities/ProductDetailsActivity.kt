package com.cdj.firestore.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.cdj.firestore.R
import com.cdj.firestore.firestore.FirestoreClass
import com.cdj.firestore.models.CartItem
import com.cdj.firestore.models.Product
import com.cdj.firestore.utils.Constants
import com.cdj.firestore.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_product_details.*

class ProductDetailsActivity : BaseActivity() {

    private var mProductID: String = ""
    private lateinit var mProductDetails: Product

    private var mproductOwnerId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_details)
        setupActionBar()

        if (intent.hasExtra(Constants.EXTRA_PRODUCT_ID)) {
            mProductID = intent.getStringExtra(Constants.EXTRA_PRODUCT_ID)!!
        }



        if (intent.hasExtra(Constants.EXTRA_PRODUCT_OWNER_ID)) {
            mproductOwnerId = intent.getStringExtra(Constants.EXTRA_PRODUCT_OWNER_ID)!!
        }

        if (FirestoreClass().getCurrentUserID() == mproductOwnerId) {
            btn_add_to_cart.visibility = View.GONE
            btn_go_to_cart.visibility = View.GONE
        } else {
            btn_add_to_cart.visibility = View.VISIBLE
            btn_go_to_cart.visibility = View.VISIBLE
        }

        configClicks()
    }

    private fun configClicks() {
        btn_add_to_cart.setOnClickListener { addToCart() }
        btn_go_to_cart.setOnClickListener {
            startActivity(
                Intent(
                    this@ProductDetailsActivity,
                    CartListActivity::class.java
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        getProductDetails()
    }

    private fun getProductDetails() {
        showProgressDialog(null)
        FirestoreClass().getProductDetails(this, mProductID)
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_product_details_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_product_details_activity.setNavigationOnClickListener { onBackPressed() }
    }

    fun productDetailsSuccess(product: Product) {
        mProductDetails = product

        GlideLoader(this).loadProductPicture(product.image, iv_product_detail_image, null)
        tv_product_details_title.text = product.title
        tv_product_details_price.text = product.price
        tv_product_details_description.text = product.description
        tv_product_details_stock_quantity.text = product.stock_quantity

        if (product.stock_quantity.toInt() <= 0) {
            hideProgressDialog()

            btn_add_to_cart.visibility = View.GONE
            tv_product_details_stock_quantity.text = getString(R.string.lbl_out_of_stock)
            tv_product_details_stock_quantity.setTextColor(
                ContextCompat.getColor(
                    this@ProductDetailsActivity,
                    R.color.colorSnackBarError
                )
            )
        } else {
            if (FirestoreClass().getCurrentUserID() == product.user_id) {
                hideProgressDialog()
            } else {
                FirestoreClass().checkIfItemExistInCart(this@ProductDetailsActivity, product.id)
            }
        }

    }

    private fun addToCart() {
        if (mProductDetails.user_id == FirestoreClass().getCurrentUserID()) {
            showErrorSnackBar("You can't buy your own product!", true)
            return
        }
        val cartItem = CartItem(
            FirestoreClass().getCurrentUserID(),
            mProductID,
            mproductOwnerId,
            mProductDetails.title,
            mProductDetails.price,
            mProductDetails.image,
            Constants.DEFAULT_CART_QUANTITY
        )
        showProgressDialog(null)
        FirestoreClass().addCartItems(this@ProductDetailsActivity, cartItem)
    }

    fun productExistsInCart() {
        hideProgressDialog()
        btn_add_to_cart.visibility = View.GONE
    }

    fun productNotInCart() {
        hideProgressDialog()
        btn_add_to_cart.visibility = View.VISIBLE
    }

    fun addToCartSuccess() {
        hideProgressDialog()

        btn_add_to_cart.visibility = View.GONE
        showErrorSnackBar(getString(R.string.success_message_item_added_to_cart), false)
    }
}