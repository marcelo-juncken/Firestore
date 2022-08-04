package com.cdj.firestore.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.GridLayoutManager
import com.cdj.firestore.R
import com.cdj.firestore.adapters.DashboardItemsListAdapter
import com.cdj.firestore.databinding.FragmentDashboardBinding
import com.cdj.firestore.firestore.FirestoreClass
import com.cdj.firestore.models.Product
import com.cdj.firestore.ui.activities.CartListActivity
import com.cdj.firestore.ui.activities.ProductDetailsActivity
import com.cdj.firestore.ui.activities.SettingsActivity
import com.cdj.firestore.utils.Constants
import kotlinx.android.synthetic.main.fragment_dashboard.*

class DashboardFragment : BaseFragment(), DashboardItemsListAdapter.OnClickList {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        getDashboardItemsList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            R.id.action_cart -> {
                startActivity(Intent(activity,CartListActivity::class.java))
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun getDashboardItemsList() {
        showProgressDialog(null)
        FirestoreClass().getDashBoardItemsList(this@DashboardFragment)
    }

    fun successDashboardItemsList(dashboardItemsList: ArrayList<Product>) {
        hideProgressDialog()
        if (dashboardItemsList.size > 0) {
            rv_dashboard_items.visibility = View.VISIBLE
            tv_no_dashboard_items_found.visibility = View.GONE

            rv_dashboard_items.layoutManager = GridLayoutManager(requireContext(), 2)
            rv_dashboard_items.setHasFixedSize(true)
            val myProductsListAdapter =
                DashboardItemsListAdapter(requireContext(), dashboardItemsList, this)
            rv_dashboard_items.adapter = myProductsListAdapter

        } else {
            rv_dashboard_items.visibility = View.GONE
            tv_no_dashboard_items_found.visibility = View.VISIBLE
        }
    }

    override fun onClick(product: Product) {
        val intent = Intent(context, ProductDetailsActivity::class.java)
        intent.putExtra(Constants.EXTRA_PRODUCT_ID, product.id)
        intent.putExtra(Constants.EXTRA_PRODUCT_OWNER_ID, product.user_id)
        startActivity(intent)
    }


}