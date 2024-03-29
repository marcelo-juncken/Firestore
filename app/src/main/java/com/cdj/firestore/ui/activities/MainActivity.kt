package com.cdj.firestore.ui.activities

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cdj.firestore.R
import com.cdj.firestore.utils.Constants
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences = getSharedPreferences(
            Constants.MYSHOPAPP_PREFERENCES,
            Context.MODE_PRIVATE
        )

        val username = sharedPreferences.getString(
            Constants.LOGGED_IN_USERNAME,
            ""
        )!!
        tv_main.text = "Hello, $username."
    }
}