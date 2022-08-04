package com.cdj.firestore.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cdj.firestore.R
import com.cdj.firestore.models.Product
import com.cdj.firestore.ui.activities.ProductDetailsActivity
import com.cdj.firestore.utils.Constants
import com.cdj.firestore.utils.GlideLoader
import kotlinx.android.synthetic.main.item_list_layout.view.*

class MyProductsListAdapter(
    private val context: Context,
    private val list: ArrayList<Product>,
    private val onClickLIST: onClickList
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_list_layout, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val product = list[position]

        if (holder is MyViewHolder) {
            GlideLoader(context).loadProductPicture(
                product.image,
                holder.itemView.iv_item_image,
                null
            )
            holder.itemView.tv_item_name.text = product.title
            "$${product.price}".also { holder.itemView.tv_item_price.text = it }

            holder.itemView.ib_delete_product.setOnClickListener { onClickLIST.onClick(product.id) }

            holder.itemView.setOnClickListener {
                val intent = Intent(context, ProductDetailsActivity::class.java)
                intent.putExtra(Constants.EXTRA_PRODUCT_ID, product.id)
                intent.putExtra(Constants.EXTRA_PRODUCT_OWNER_ID, product.user_id)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface onClickList {
        fun onClick(productID: String)
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}