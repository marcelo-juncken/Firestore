package com.cdj.firestore.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.cdj.firestore.adapters.MyOrdersListAdapter
import com.cdj.firestore.databinding.FragmentOrdersBinding
import com.cdj.firestore.firestore.FirestoreClass
import com.cdj.firestore.models.Order
import kotlinx.android.synthetic.main.fragment_orders.*

class OrdersFragment : BaseFragment() {

    private var _binding: FragmentOrdersBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentOrdersBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        getMyOrdersList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getMyOrdersList() {
        showProgressDialog(null)
        FirestoreClass().getMyOrdersList(this@OrdersFragment)
    }

    fun populateOrdersListInUI(ordersList: ArrayList<Order>) {
        hideProgressDialog()

        if (ordersList.size > 0) {
            tv_no_orders_found.visibility = View.GONE
            rv_my_order_items.visibility = View.VISIBLE

            rv_my_order_items.layoutManager = LinearLayoutManager(requireContext())
            rv_my_order_items.setHasFixedSize(true)
            val myOrderListAdapter = MyOrdersListAdapter(requireActivity(),ordersList)
            rv_my_order_items.adapter = myOrderListAdapter
        } else {
            tv_no_orders_found.visibility = View.VISIBLE
            rv_my_order_items.visibility = View.GONE
        }
    }
}