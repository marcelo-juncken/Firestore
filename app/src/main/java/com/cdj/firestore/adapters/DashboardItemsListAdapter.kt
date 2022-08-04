package com.cdj.firestore.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cdj.firestore.R
import com.cdj.firestore.models.Product
import com.cdj.firestore.utils.GlideLoader
import kotlinx.android.synthetic.main.item_dashboard_layout.view.*

class DashboardItemsListAdapter(
    private val context: Context,
    private val list: ArrayList<Product>,
    private val onClickListener: OnClickList
)

    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_dashboard_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val product = list[position]

        if (holder is MyViewHolder) {
            GlideLoader(context).loadProductPicture(
                product.image,
                holder.itemView.iv_dashboard_item_image,
                null
            )
            holder.itemView.tv_dashboard_item_title.text = product.title
            "$${product.price}".also { holder.itemView.tv_dashboard_item_price.text = it }

            holder.itemView.setOnClickListener { onClickListener.onClick(product) }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnClickList {
        fun onClick(product: Product)
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}