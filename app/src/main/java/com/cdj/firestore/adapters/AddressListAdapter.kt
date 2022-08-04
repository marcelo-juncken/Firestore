package com.cdj.firestore.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cdj.firestore.R
import com.cdj.firestore.firestore.FirestoreClass
import com.cdj.firestore.models.Address
import com.cdj.firestore.ui.activities.AddEditAddressActivity
import com.cdj.firestore.ui.activities.AddressListActivity
import com.cdj.firestore.utils.Constants
import kotlinx.android.synthetic.main.item_address_layout.view.*

class AddressListAdapter(
    private val context: Context,
    private val list: ArrayList<Address>,
    private val onClickItem: OnClickItem
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_address_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            holder.itemView.tv_address_full_name.text = model.name
            holder.itemView.tv_address_type.text = model.type
            "${model.address}, ${model.zip_code}".also {
                holder.itemView.tv_address_details.text = it
            }
            holder.itemView.tv_address_mobile_number.text = model.mobile_number

            holder.itemView.setOnClickListener { onClickItem.onClick(model) }
        }
    }

    fun notifyEditItem(activity: Activity, position: Int) {
        val intent = Intent(context, AddEditAddressActivity::class.java)
        intent.putExtra(Constants.EXTRA_ADDRESS_DETAILS, list[position])
        activity.startActivity(intent)
        notifyItemChanged(position)
    }


    override fun getItemCount(): Int {
        return list.size
    }

    fun notifyDeleteItem(activity: AddressListActivity, position: Int) {
        FirestoreClass().deleteAddress(
            activity,
            list[position].id)

    }

    interface OnClickItem{
        fun onClick(address: Address)
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}