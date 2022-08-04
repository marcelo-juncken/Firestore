package com.cdj.firestore.ui.activities

import android.content.Intent
import android.os.Bundle
import com.cdj.firestore.R
import com.cdj.firestore.firestore.FirestoreClass
import com.cdj.firestore.models.User
import com.cdj.firestore.utils.Constants
import com.cdj.firestore.utils.GlideLoader
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : BaseActivity() {

    private lateinit var mUserDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupActionBar()
        configClicks()
    }

    private fun configClicks() {
        tv_edit.setOnClickListener {
            val intent = Intent(this@SettingsActivity, UserProfileActivity::class.java)
            intent.putExtra(Constants.EXTRA_USER_DETAILS, mUserDetails)
            startActivity(intent)
        }

        btn_logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        ll_address.setOnClickListener {
            val intent = Intent(this@SettingsActivity, AddressListActivity::class.java)
            startActivity(intent)
        }

    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_settings_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_settings_activity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun getUserDetails() {
        showProgressDialog(null)

        FirestoreClass().getUserDetails(this@SettingsActivity)
    }

    fun userDetailsSuccess(user: User) {
        mUserDetails = user

        hideProgressDialog()
        GlideLoader(baseContext).loadUserPicture(user.image, iv_user_photo) {
            tv_name.text = buildString {
                append(user.firstName)
                append(" ")
                append(user.lastName)
            }
            tv_gender.text = user.gender
            tv_email.text = user.email
            tv_mobile_number.text = "${user.mobile}"
        }

    }

    override fun onResume() {
        super.onResume()
        getUserDetails()
    }
}