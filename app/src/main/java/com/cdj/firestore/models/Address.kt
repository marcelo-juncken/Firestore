package com.cdj.firestore.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Address(
    val user_id : String = "",
    val name : String = "",
    val mobile_number : String = "",

    val address : String = "",
    val zip_code : String = "",
    val additional_note : String = "",

    val type : String = "",
    val other_details : String = "",
    var id : String = ""
): Parcelable
