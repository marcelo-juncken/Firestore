package com.cdj.firestore.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cdj.firestore.R
import com.cdj.firestore.models.Order
import com.cdj.firestore.ui.activities.MyOrderDetailsActivity
import com.cdj.firestore.utils.Constants
import com.cdj.firestore.utils.GlideLoader
import kotlinx.android.synthetic.main.item_list_layout.view.*

class MyOrdersListAdapter(
    private val context: Context,
    private val list: ArrayList<Order>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_list_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val order = list[position]

        if (holder is MyViewHolder) {

            holder.itemView.ib_delete_product.visibility = View.GONE
            holder.itemView.tv_item_name.text = order.title
            "$${order.total_amount}".also { holder.itemView.tv_item_price.text = it }
            GlideLoader(context).loadProductPicture(
                order.image,
                holder.itemView.iv_item_image,
                null
            )

            holder.itemView.setOnClickListener {
                val intent = Intent(context, MyOrderDetailsActivity::class.java)
                intent.putExtra(Constants.EXTRA_MY_ORDER_DETAILS, order)
                context.startActivity(intent)
            }
        }
    }


    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}