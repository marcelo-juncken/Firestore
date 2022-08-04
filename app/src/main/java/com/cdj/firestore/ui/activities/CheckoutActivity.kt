package com.cdj.firestore.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.cdj.firestore.R
import com.cdj.firestore.adapters.CartItemsListAdapter
import com.cdj.firestore.firestore.FirestoreClass
import com.cdj.firestore.models.Address
import com.cdj.firestore.models.CartItem
import com.cdj.firestore.models.Order
import com.cdj.firestore.models.Product
import com.cdj.firestore.utils.Constants
import kotlinx.android.synthetic.main.activity_checkout.*

class CheckoutActivity : BaseActivity() {

    private var mAddressDetails: Address? = null
    private lateinit var mProductList: ArrayList<Product>
    private var mCartList: ArrayList<CartItem> = ArrayList()

    private var mHasAnyInStockItem = false
    private var mSubTotal: Double =0.0
    private var mTotal: Double = 0.0

    private lateinit var mOrderDetails: Order


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        setupActionBar()

        if (intent.hasExtra(Constants.EXTRA_SELECTED_ADDRESS)) {
            mAddressDetails = intent.getParcelableExtra(Constants.EXTRA_SELECTED_ADDRESS)

            if (mAddressDetails != null) {
                tv_checkout_address_type.text = mAddressDetails!!.type
                tv_checkout_full_name.text = mAddressDetails!!.name
                "${mAddressDetails!!.address}, ${mAddressDetails!!.zip_code}".also {
                    tv_checkout_address.text = it
                }
                tv_checkout_additional_note.text = mAddressDetails!!.additional_note

                if (mAddressDetails!!.type == Constants.OTHER && mAddressDetails!!.other_details.isNotEmpty()) {
                    tv_checkout_other_details.text = mAddressDetails!!.other_details
                    tv_checkout_other_details.visibility = View.VISIBLE
                } else {
                    tv_checkout_other_details.visibility = View.GONE
                }

                tv_checkout_mobile_number.text = mAddressDetails!!.mobile_number
            }
        }

        configClicks()
    }

    private fun configClicks() {
        btn_place_order.setOnClickListener {
            if(mHasAnyInStockItem){
                placeAnOrder()
            }else{
                showErrorSnackBar(getString(R.string.err_msg_no_items_in_stock), true)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getProductList()
    }

    private fun getProductList() {
        showProgressDialog(null)

        FirestoreClass().getAllProductsList(this@CheckoutActivity)
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_checkout_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_checkout_activity.setNavigationOnClickListener { onBackPressed() }
    }

    fun successProductsListFromFireStore(productsList: ArrayList<Product>) {
        mProductList = productsList
        getCartItemsList()
    }

    private fun getCartItemsList(){
        FirestoreClass().getCartList(this@CheckoutActivity)
    }

    fun successCartItemsList(cartList: ArrayList<CartItem>) {
        hideProgressDialog()

        mHasAnyInStockItem = false

        for(product in mProductList){
            for(cartItem in cartList){

                if(cartItem.product_id==product.id){
                    cartItem.stock_quantity = product.stock_quantity

                    if (product.stock_quantity.toInt() == 0) {
                        cartItem.cart_quantity = product.stock_quantity
                    } else {
                        mCartList.add(cartItem)
                        mHasAnyInStockItem = true
                    }
                }
            }
        }

        if (mCartList.size>0){
            rv_cart_list_items.layoutManager = LinearLayoutManager(this@CheckoutActivity)
            rv_cart_list_items.setHasFixedSize(true)
            val cartItemsListAdapter = CartItemsListAdapter(this@CheckoutActivity,mCartList, null, false)
            rv_cart_list_items.adapter =cartItemsListAdapter

            var shippingFee = 0.0

            for (cartItem in mCartList){
                val qtd = cartItem.cart_quantity.toDouble()
                val price = cartItem.price.toDouble()
                mSubTotal += (qtd * price)
            }

            "$$mSubTotal".also { tv_checkout_sub_total.text = it }
            if(mHasAnyInStockItem){
                shippingFee = 10.0
            }
            "$$shippingFee".also { tv_checkout_shipping_charge.text = it }
            mTotal = mSubTotal + shippingFee
            "$$mTotal".also { tv_checkout_total_amount.text = it }

        }

    }

    private fun placeAnOrder(){
        showProgressDialog(null)

        if(mAddressDetails !=null){
            val time = System.currentTimeMillis()
            mOrderDetails = Order(
                FirestoreClass().getCurrentUserID(),
                mCartList,
                mAddressDetails!!,
                "My order $time",
                mCartList[0].image,
                mSubTotal.toString(),
                "10.0",
                mTotal.toString(),
                time

            )

            FirestoreClass().placeOrder(this@CheckoutActivity,mOrderDetails)
        }



    }

    fun successOrderPlaced() {
        FirestoreClass().updateAllDetails(this@CheckoutActivity,mCartList, mOrderDetails)
    }

    fun successfullyUpdatedAllDetails() {
        hideProgressDialog()
        showErrorSnackBar(getString(R.string.msg_your_order_placed_successfully), false)

        val intent = Intent(this@CheckoutActivity, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}