package com.cdj.firestore.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.cdj.firestore.R
import com.cdj.firestore.models.CartItem
import com.cdj.firestore.utils.Constants
import com.cdj.firestore.utils.GlideLoader
import kotlinx.android.synthetic.main.item_cart_layout.view.*

class CartItemsListAdapter(
    private val context: Context,
    private val cartList: ArrayList<CartItem>,
    private val onClickListener: OnClickList?,
    private val updateCartItems: Boolean
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_cart_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val cartItem = cartList[position]

        if (holder is MyViewHolder) {
            GlideLoader(context).loadProductPicture(
                cartItem.image,
                holder.itemView.iv_cart_item_image,
                null
            )
            holder.itemView.tv_cart_item_title.text = cartItem.title
            "$${cartItem.price}".also { holder.itemView.tv_cart_item_price.text = it }


            if (cartItem.cart_quantity == "0") {
                holder.itemView.ib_remove_cart_item.visibility = View.GONE
                holder.itemView.ib_add_cart_item.visibility = View.GONE
                holder.itemView.tv_cart_quantity.text = context.getString(R.string.lbl_out_of_stock)
                holder.itemView.tv_cart_quantity.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorSnackBarError
                    )
                )
                if (updateCartItems) {
                    holder.itemView.ib_delete_cart_item.visibility = View.VISIBLE
                } else {
                    holder.itemView.ib_delete_cart_item.visibility = View.GONE
                }
            } else {

                if (updateCartItems) {
                    holder.itemView.ib_delete_cart_item.visibility = View.VISIBLE
                    holder.itemView.ib_remove_cart_item.visibility = View.VISIBLE
                    holder.itemView.ib_add_cart_item.visibility = View.VISIBLE
                } else {
                    holder.itemView.ib_delete_cart_item.visibility = View.GONE
                    holder.itemView.ib_remove_cart_item.visibility = View.GONE
                    holder.itemView.ib_add_cart_item.visibility = View.GONE
                }


                holder.itemView.tv_cart_quantity.text = cartItem.cart_quantity
                holder.itemView.tv_cart_quantity.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorSecondaryText
                    )
                )

            }

            holder.itemView.ib_delete_cart_item.setOnClickListener {
                onClickListener?.onClick(
                    cartItem,
                    Constants.DELETE_CART_ITEM
                )
            }

            holder.itemView.ib_remove_cart_item.setOnClickListener {
                onClickListener?.onClick(
                    cartItem,
                    Constants.REMOVE_CART_ITEM
                )
            }
            holder.itemView.ib_add_cart_item.setOnClickListener {
                onClickListener?.onClick(
                    cartItem,
                    Constants.ADD_CART_ITEM
                )
            }


        }

    }

    override fun getItemCount(): Int {
        return cartList.size
    }

    interface OnClickList {
        fun onClick(cartItem: CartItem, typeClicked: String)
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}