package com.cdj.firestore.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cdj.firestore.R
import com.cdj.firestore.adapters.AddressListAdapter
import com.cdj.firestore.firestore.FirestoreClass
import com.cdj.firestore.models.Address
import com.cdj.firestore.utils.Constants
import com.cdj.firestore.utils.SwipeToDeleteCallback
import com.cdj.firestore.utils.SwipeToEditCallback
import kotlinx.android.synthetic.main.activity_address_list.*

class AddressListActivity : BaseActivity(), AddressListAdapter.OnClickItem {

    private var mSelectAddress: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address_list)
        setupActionBar()

        if (intent.hasExtra(Constants.EXTRA_SELECT_ADDRESS)){
            mSelectAddress = intent.getBooleanExtra(Constants.EXTRA_SELECT_ADDRESS, false)

            if(mSelectAddress){
                tv_title.text = getString(R.string.title_select_address)

            }
        }

        configClicks()
    }

    override fun onResume() {
        super.onResume()
        getAddressList()
    }

    private fun configClicks() {
        tv_add_address.setOnClickListener {
            val intent = Intent(this@AddressListActivity, AddEditAddressActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_address_list_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_address_list_activity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun getAddressList() {
        showProgressDialog(null)
        FirestoreClass().getAddressesList(this@AddressListActivity)
    }

    fun successAddressListFromFirestore(addressList: ArrayList<Address>) {
        hideProgressDialog()


        if (addressList.size > 0) {
            tv_no_address_found.visibility = View.GONE
            rv_address_list.visibility = View.VISIBLE

            rv_address_list.layoutManager = LinearLayoutManager(this@AddressListActivity)
            rv_address_list.setHasFixedSize(true)
            val addressAdapter = AddressListAdapter(this@AddressListActivity, addressList, this@AddressListActivity)


            val editSwipeHandler = object : SwipeToEditCallback(this) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val adapter = rv_address_list.adapter as AddressListAdapter
                    adapter.notifyEditItem(
                        this@AddressListActivity,
                        viewHolder.adapterPosition
                    )
                }
            }
            val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
            editItemTouchHelper.attachToRecyclerView(rv_address_list)


            val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    showProgressDialog(null)
                    val adapter = rv_address_list.adapter as AddressListAdapter
                    adapter.notifyDeleteItem(
                        this@AddressListActivity,
                        viewHolder.adapterPosition
                    )

                }
            }
            val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
            deleteItemTouchHelper.attachToRecyclerView(rv_address_list)

            rv_address_list.adapter = addressAdapter
        } else {
            tv_no_address_found.visibility = View.VISIBLE
            rv_address_list.visibility = View.GONE
        }
    }

    fun successAddressDelete() {
        FirestoreClass().getAddressesList(this@AddressListActivity)
    }

    override fun onClick(address: Address) {
        if(mSelectAddress){
            val intent = Intent(this@AddressListActivity, CheckoutActivity::class.java)
            intent.putExtra(Constants.EXTRA_SELECTED_ADDRESS, address)
            startActivity(intent)
        }
    }
}