package com.cdj.firestore.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import com.cdj.firestore.R
import com.cdj.firestore.firestore.FirestoreClass
import com.cdj.firestore.models.User
import com.cdj.firestore.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        tv_register.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        btn_login.setOnClickListener { loginUser() }

        tv_forgot_password.setOnClickListener {
            val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

    }

    private fun validateLoginDetails(): Boolean {
        return when {
            et_email.text.toString().trim().isEmpty() -> {
                showErrorSnackBar("", true)
                false
            }
            et_password.text.toString().trim().isEmpty() -> {
                showErrorSnackBar("", true)
                false
            }
            else -> {
                true
            }
        }
    }

    private fun loginUser() {
        if (validateLoginDetails()) {

            showProgressDialog(null)
            val email = et_email.text.toString().trim()
            val password = et_password.text.toString().trim()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(
                email, password
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    FirestoreClass().getUserDetails(this@LoginActivity)
                } else {
                    hideProgressDialog()
                    showErrorSnackBar(task.exception!!.message.toString(), true)
                }
            }
        }
    }

    fun userLoggedInSuccess(user: User) {
        hideProgressDialog()

        if (user.profileCompleted == 0) {
            val intent = Intent(this@LoginActivity, UserProfileActivity::class.java)
            intent.putExtra(Constants.EXTRA_USER_DETAILS, user)
            startActivity(intent)

        } else {
            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
        }
        finish()
    }
}