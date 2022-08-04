package com.cdj.firestore.ui.activities

import android.os.Bundle
import android.view.View
import com.cdj.firestore.R
import com.cdj.firestore.firestore.FirestoreClass
import com.cdj.firestore.models.Address
import com.cdj.firestore.utils.Constants
import kotlinx.android.synthetic.main.activity_add_edit_address.*

class AddEditAddressActivity : BaseActivity() {

    private var mAddressDetails: Address? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_address)
        setupActionBar()

        if (intent.hasExtra(Constants.EXTRA_ADDRESS_DETAILS)) {
            mAddressDetails = intent.getParcelableExtra(Constants.EXTRA_ADDRESS_DETAILS)

            if (mAddressDetails != null) {
                if (mAddressDetails!!.id.isNotEmpty()) {
                    et_full_name.setText(mAddressDetails!!.name)
                    et_phone_number.setText(mAddressDetails!!.mobile_number)
                    et_address.setText(mAddressDetails!!.address)
                    et_zip_code.setText(mAddressDetails!!.zip_code)
                    et_additional_note.setText(mAddressDetails!!.additional_note)

                    when (mAddressDetails!!.type) {
                        Constants.HOME -> {
                            rb_home.isChecked = true
                        }
                        Constants.OFFICE -> {
                            rb_office.isChecked = true
                        }
                        Constants.OTHER -> {
                            rb_other.isChecked = true
                            til_other_details.visibility = View.VISIBLE
                        }
                    }
                    et_other_details.setText(mAddressDetails!!.other_details)

                }

            }
        }
        configClicks()
    }

    private fun configClicks() {
        btn_submit_address.setOnClickListener { saveAddressToFirestore() }

        rg_type.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rb_other) {
                til_other_details.visibility = View.VISIBLE
            } else {
                til_other_details.visibility = View.GONE
            }
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_add_edit_address_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_add_edit_address_activity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun validateAddressData(): Boolean {
        when {
            et_full_name.text.toString().trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_please_enter_full_name), true)
                return false
            }
            et_phone_number.text.toString().trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_please_enter_phone_number), true)
                return false
            }
            et_address.text.toString().trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_please_enter_address), true)
                return false
            }
            et_zip_code.text.toString().trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_please_enter_zip_code), true)
                return false
            }
            rb_other.isChecked && et_other_details.text.toString().trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_please_enter_other_details), true)
                return false
            }
            else -> {
                return true
            }
        }
    }

    private fun saveAddressToFirestore() {
        val fullName: String = et_full_name.text.toString().trim()
        val phoneNumber: String = et_phone_number.text.toString().trim()
        val address: String = et_address.text.toString().trim()
        val zipCode: String = et_zip_code.text.toString().trim()
        val additionalNote: String = et_additional_note.text.toString().trim()
        val otherDetails: String = et_other_details.text.toString().trim()

        if (validateAddressData()) {

            showProgressDialog(null)

            val addressType: String = when {
                rb_home.isChecked -> {
                    Constants.HOME
                }
                rb_office.isChecked -> {
                    Constants.OFFICE
                }
                else -> {
                    Constants.OTHER
                }
            }

            val addressModel = Address(
                FirestoreClass().getCurrentUserID(),
                fullName,
                phoneNumber,
                address,
                zipCode,
                additionalNote,
                addressType,
                otherDetails
            )

            if(mAddressDetails != null && mAddressDetails!!.id.isNotEmpty()){
                FirestoreClass().updateAddress(this@AddEditAddressActivity,addressModel,mAddressDetails!!.id)
            } else {
                FirestoreClass().addAddress(this@AddEditAddressActivity, addressModel)
            }

        }
    }

    fun successAddressAddUpdate() {
        hideProgressDialog()
        finish()
    }
}