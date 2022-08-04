package com.cdj.firestore.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.cdj.firestore.R
import com.cdj.firestore.adapters.CartItemsListAdapter
import com.cdj.firestore.firestore.FirestoreClass
import com.cdj.firestore.models.CartItem
import com.cdj.firestore.models.Product
import com.cdj.firestore.utils.Constants
import kotlinx.android.synthetic.main.activity_cart_list.*

class CartListActivity : BaseActivity(), CartItemsListAdapter.OnClickList {

    private lateinit var mProductsList: ArrayList<Product>
    private lateinit var mCartListItems: ArrayList<CartItem>
    private var hasAnyInStockItem = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart_list)
        setupActionBar()

        configClicks()
    }

    private fun configClicks() {
        btn_checkout.setOnClickListener {
            if(hasAnyInStockItem){
                val intent = Intent(this@CartListActivity, AddressListActivity::class.java)
                intent.putExtra(Constants.EXTRA_SELECT_ADDRESS, true)
                startActivity(intent)
            } else {
                showErrorSnackBar(getString(R.string.err_msg_no_items_in_stock), true)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getProductList()
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_cart_list_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_cart_list_activity.setNavigationOnClickListener { onBackPressed() }
    }

    fun successCartItemsList(cartList: ArrayList<CartItem>) {
        hasAnyInStockItem = false
        hideProgressDialog()

        for (product in mProductsList) {
            for (cartItem in cartList) {
                if (product.id == cartItem.product_id) {

                    cartItem.stock_quantity = product.stock_quantity

                    if (product.stock_quantity.toInt() == 0) {
                        cartItem.cart_quantity = product.stock_quantity
                    } else {
                        hasAnyInStockItem = true
                    }
                }
            }
        }

        mCartListItems = cartList

        if (mCartListItems.size > 0) {
            tv_no_cart_item_found.visibility = View.GONE
            rv_cart_items_list.visibility = View.VISIBLE
            ll_checkout.visibility = View.VISIBLE

            rv_cart_items_list.layoutManager = LinearLayoutManager(this@CartListActivity)
            rv_cart_items_list.setHasFixedSize(true)
            val cartListAdapter = CartItemsListAdapter(this@CartListActivity, cartList, this, true)
            rv_cart_items_list.adapter = cartListAdapter

            var subTotal = 0.0
            var shippingFee = 0.0

            for (item in mCartListItems) {
                val availableQuantity = item.stock_quantity.toInt()
                if (availableQuantity > 0) {
                    val price = item.price.toDouble()
                    val quantity = item.cart_quantity.toDouble()
                    subTotal += (price * quantity)
                }
            }

            "$$subTotal".also { tv_sub_total.text = it }
            if(hasAnyInStockItem){
                shippingFee = 10.0
            }
            "$$shippingFee".also { tv_shipping_charge.text = it }
            val total = subTotal + shippingFee
            "$$total".also { tv_total_amount.text = it }


        } else {
            tv_no_cart_item_found.visibility = View.VISIBLE
            ll_checkout.visibility = View.GONE
            rv_cart_items_list.visibility = View.GONE

        }
    }

    fun successDeletedItem() {
        showErrorSnackBar(getString(R.string.msg_item_removed_successfully), false)
        getCartItemsList()
    }

    private fun getCartItemsList() {
        FirestoreClass().getCartList(this@CartListActivity)
    }

    override fun onClick(cartItem: CartItem, typeClicked: String) {
        val cartQuantity: Int = cartItem.cart_quantity.toInt()
        val hashItemCart: HashMap<String, Any> = HashMap()

        when (typeClicked) {
            Constants.DELETE_CART_ITEM -> {
                showProgressDialog(null)
                FirestoreClass().deleteItemFromCart(this@CartListActivity, cartItem.id)
            }
            Constants.ADD_CART_ITEM -> {
                if (cartItem.stock_quantity.toInt() > cartQuantity) {
                    showProgressDialog(null)
                    hashItemCart[Constants.CART_QUANTITY] = (cartQuantity + 1).toString()

                    FirestoreClass().updateMyCart(this@CartListActivity, cartItem.id, hashItemCart)
                } else {
                    showErrorSnackBar(
                        getString(
                            R.string.msg_for_available_stock,
                            cartItem.stock_quantity
                        ), true
                    )
                }
            }
            Constants.REMOVE_CART_ITEM -> {
                if (cartQuantity > 1) {
                    showProgressDialog(null)
                    hashItemCart[Constants.CART_QUANTITY] = (cartQuantity - 1).toString()

                    FirestoreClass().updateMyCart(this@CartListActivity, cartItem.id, hashItemCart)
                } else {
                    showProgressDialog(null)
                    FirestoreClass().deleteItemFromCart(this@CartListActivity, cartItem.id)
                }
            }
        }
    }


    private fun getProductList() {
        showProgressDialog(null)
        FirestoreClass().getAllProductsList(this@CartListActivity)
    }

    fun successProductsListFromFireStore(productsList: ArrayList<Product>) {
        mProductsList = productsList

        getCartItemsList()
    }

    fun successUpdatedItem() {
        FirestoreClass().getAllProductsList(this@CartListActivity)
    }


}