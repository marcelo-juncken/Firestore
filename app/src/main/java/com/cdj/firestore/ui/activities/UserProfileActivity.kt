package com.cdj.firestore.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.cdj.firestore.R
import com.cdj.firestore.firestore.FirestoreClass
import com.cdj.firestore.models.User
import com.cdj.firestore.utils.Constants
import com.cdj.firestore.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_user_profile.*
import java.io.IOException

class UserProfileActivity : BaseActivity() {

    private lateinit var mUserDetails: User
    private var mSelectedImageFileUri: Uri? = null
    private var mUserProfileImageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)



        if (intent.hasExtra(Constants.EXTRA_USER_DETAILS)) mUserDetails =
            intent.getParcelableExtra(Constants.EXTRA_USER_DETAILS)!!

        et_first_name.setText(mUserDetails.firstName)
        et_last_name.setText(mUserDetails.lastName)
        et_email.setText(mUserDetails.email)
        et_email.isEnabled = false

        if (mUserDetails.profileCompleted == 0) {
            tv_title.text = getString(R.string.title_complete_profile)

            et_first_name.isEnabled = false
            et_last_name.isEnabled = false
        } else {
            setupActionBar()
            tv_title.text = getString(R.string.title_edit_profile)
            GlideLoader(baseContext).loadUserPicture(mUserDetails.image, iv_user_photo) {

                if (mUserDetails.mobile != 0L) {
                    et_mobile_number.setText(mUserDetails.mobile.toString())
                }
                if (mUserDetails.gender == Constants.MALE) {
                    rb_male.isChecked = true
                } else {
                    rb_female.isChecked = true
                }
            }
        }

        configClicks()
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_user_profile_activity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        toolbar_user_profile_activity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun configClicks() {
        iv_user_photo.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this@UserProfileActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(this@UserProfileActivity)
            } else {
                ActivityCompat.requestPermissions(
                    this@UserProfileActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        btn_submit.setOnClickListener { saveUserDetails() }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            //If permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this@UserProfileActivity)
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(
                    this,
                    resources.getString(R.string.read_storage_permission_denied),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.PICK_IMAGE_REQUEST_CODE) {
                if (data != null) {
                    try {

                        mSelectedImageFileUri = data.data!!


                        GlideLoader(this@UserProfileActivity).loadUserPicture(
                            mSelectedImageFileUri!!,
                            iv_user_photo, null
                        )

                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@UserProfileActivity,
                            resources.getString(R.string.image_selection_failed),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            // A log is printed when user close or cancel the image selection.
            Log.e("Request Cancelled", "Image selection cancelled")
        }
    }

    private fun validateUserProfileDetails(): Boolean {
        return when {
            et_first_name.text.toString().trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_first_name), true)
                false
            }
            et_last_name.text.toString().trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_last_name), true)
                false
            }
            et_mobile_number.text.toString().trim().isEmpty() -> {
                showErrorSnackBar(getString(R.string.err_msg_enter_mobile_number), true)
                false
            }
            rg_gender.checkedRadioButtonId == -1 -> {
                showErrorSnackBar(getString(R.string.err_msg_check_gender), true)
                false
            }
            else -> {
                true
            }
        }
    }

    private fun saveUserDetails() {
        if (validateUserProfileDetails()) {
            showProgressDialog(getString(R.string.please_wait))

            if (mSelectedImageFileUri != null) {

                FirestoreClass().uploadImageToCloudStorage(
                    this@UserProfileActivity,
                    mSelectedImageFileUri,
                Constants.USER_PROFILE_IMAGE)
            } else {
                updateUserProfileDetails()
            }
        }
    }

    fun userProfileUpdateSuccess() {
        // Hide the progress dialog
        hideProgressDialog()

        showErrorSnackBar(getString(R.string.msg_profile_update_success), false)

        startActivity(Intent(this@UserProfileActivity, DashboardActivity::class.java))
        finish()
    }

    fun imageUploadSuccess(imageURL: String) {
        mUserProfileImageUrl = imageURL
        updateUserProfileDetails()
    }

    private fun updateUserProfileDetails() {
        val userHashMap = HashMap<String, Any>()

        val firstName = et_first_name.text.toString().trim()
        if (firstName != mUserDetails.firstName) {
            userHashMap[Constants.FIRST_NAME] = firstName
        }

        val lastName = et_last_name.text.toString().trim()
        if (lastName != mUserDetails.lastName) {
            userHashMap[Constants.LAST_NAME] = lastName
        }

        val mobileNumber = et_mobile_number.text.toString().trim()
        if (mobileNumber != mUserDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = mobileNumber.toLong()
        }

        val gender = if (rb_male.isChecked) {
            Constants.MALE
        } else {
            Constants.FEMALE
        }
        if(gender != mUserDetails.gender){
            userHashMap[Constants.GENDER] = gender
        }

        if (mUserProfileImageUrl.isNotEmpty())
            userHashMap[Constants.IMAGE] = mUserProfileImageUrl

        userHashMap[Constants.COMPLETE_PROFILE] = 1
        FirestoreClass().updateUserProfileData(this@UserProfileActivity, userHashMap)
    }
}

