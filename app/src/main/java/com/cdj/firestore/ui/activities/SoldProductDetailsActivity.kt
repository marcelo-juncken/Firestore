package com.cdj.firestore.ui.activities

import android.os.Bundle
import android.view.View
import com.cdj.firestore.R
import com.cdj.firestore.models.SoldProduct
import com.cdj.firestore.utils.Constants
import com.cdj.firestore.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_sold_product_details.*
import java.text.SimpleDateFormat
import java.util.*

class SoldProductDetailsActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sold_product_details)

        var productDetails = SoldProduct()

        if (intent.hasExtra(Constants.EXTRA_SOLD_PRODUCT_DETAILS)) {
            productDetails =
                intent.getParcelableExtra(Constants.EXTRA_SOLD_PRODUCT_DETAILS)!!
        }

        setupActionBar()

        setupUI(productDetails)

    }

    private fun setupActionBar() {

        setSupportActionBar(toolbar_sold_product_details_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_sold_product_details_activity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupUI(productDetails: SoldProduct) {

        tv_sold_product_details_id.text = productDetails.order_id

        val dateFormat = "dd MMM yyyy HH:mm"

        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())

        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = productDetails.order_date
        tv_sold_product_details_date.text = formatter.format(calendar.time)

        GlideLoader(this@SoldProductDetailsActivity).loadProductPicture(
            productDetails.image,
            iv_product_item_image, null
        )
        tv_product_item_name.text = productDetails.title
        "$${productDetails.price}".also { tv_product_item_price.text = it }
        tv_sold_product_quantity.text = productDetails.sold_quantity

        tv_sold_details_address_type.text = productDetails.address.type
        tv_sold_details_full_name.text = productDetails.address.name
        "${productDetails.address.address}, ${productDetails.address.zip_code}".also { tv_sold_details_address.text = it }
        tv_sold_details_additional_note.text = productDetails.address.additional_note

        if (productDetails.address.other_details.isNotEmpty()) {
            tv_sold_details_other_details.visibility = View.VISIBLE
            tv_sold_details_other_details.text = productDetails.address.other_details
        } else {
            tv_sold_details_other_details.visibility = View.GONE
        }
        tv_sold_details_mobile_number.text = productDetails.address.mobile_number

        tv_sold_product_sub_total.text = productDetails.sub_total_amount
        tv_sold_product_shipping_charge.text = productDetails.shipping_charge
        tv_sold_product_total_amount.text = productDetails.total_amount
    }

}
