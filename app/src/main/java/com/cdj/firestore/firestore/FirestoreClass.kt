package com.cdj.firestore.firestore

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import androidx.fragment.app.Fragment
import com.cdj.firestore.models.*
import com.cdj.firestore.ui.activities.*
import com.cdj.firestore.ui.fragments.DashboardFragment
import com.cdj.firestore.ui.fragments.OrdersFragment
import com.cdj.firestore.ui.fragments.ProductsFragment
import com.cdj.firestore.ui.fragments.SoldProductsFragment
import com.cdj.firestore.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class FirestoreClass {

    private val mFireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: RegisterActivity, userInfo: User) {

        // The "users" is collection name. If the collection is already created then it will not create the same one again.
        mFireStore.collection(Constants.USERS)
            // Document ID for users fields. Here the document it is the User ID.
            .document(userInfo.id)
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge later on instead of replacing the fields.
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.userRegistrationSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while registering the user.",
                    e
                )
            }
    }

    fun getCurrentUserID(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser

        var currentUserId = ""
        if (currentUser != null) {
            currentUserId = currentUser.uid
        }
        return currentUserId
    }


    fun getUserDetails(activity: Activity) {

        if (getCurrentUserID().isEmpty()) {
            when (activity) {
                is SettingsActivity -> {
                    activity.hideProgressDialog()
                }
            }
            activity.startActivity(Intent(activity, LoginActivity::class.java))
            activity.finish()
            return
        }
        // Here we pass the collection name from which we wants the data.
        mFireStore.collection(Constants.USERS)
            // The document id to get the Fields of user.
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->

                // Here we have received the document snapshot which is converted into the User Data model object.
                val user = document.toObject(User::class.java)!!

                val sharedPreferences =
                    activity.getSharedPreferences(
                        Constants.MYSHOPAPP_PREFERENCES,
                        Context.MODE_PRIVATE
                    )

                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                editor.putString(
                    Constants.LOGGED_IN_USERNAME,
                    "${user.firstName} ${user.lastName}"
                )

                editor.apply()
                when (activity) {
                    is LoginActivity -> {
                        // Call a function of base activity for transferring the result to it.
                        activity.userLoggedInSuccess(user)
                    }
                    is SettingsActivity -> {
                        activity.userDetailsSuccess(user)
                    }
                }
            }
            .addOnFailureListener { e ->
                // Hide the progress dialog if there is any error. And print the error in log.
                when (activity) {
                    is LoginActivity -> {
                        activity.hideProgressDialog()
                    }
                    is SettingsActivity -> {
                        activity.hideProgressDialog()
                    }
                }

                Log.e(
                    activity.javaClass.simpleName,
                    "Error while getting user details.",
                    e
                )
            }
    }

    fun updateUserProfileData(activity: UserProfileActivity, userHashMap: HashMap<String, Any>) {

        // The "users" is collection name. If the collection is already created then it will not create the same one again.
        mFireStore.collection(Constants.USERS)
            // Document ID for users fields. Here the document it is the User ID.
            .document(getCurrentUserID())
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge later on instead of replacing the fields.
            .update(userHashMap)
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.userProfileUpdateSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                activity.showErrorSnackBar(e.message.toString(), true)
            }
    }

    fun uploadImageToCloudStorage(activity: Activity, imageFileUri: Uri?, imageType: String) {
        val sRef: StorageReference = FirebaseStorage.getInstance().reference
            .child(
                imageType + System.currentTimeMillis() + "." + Constants.getFileExtension(
                    activity,
                    imageFileUri
                )
            )

        sRef.putFile(imageFileUri!!).addOnSuccessListener { taskSnapshot ->

            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                when (activity) {
                    is UserProfileActivity -> {
                        activity.imageUploadSuccess(uri.toString())
                    }
                    is AddProductActivity -> {
                        activity.imageUploadSuccess(uri.toString())
                    }
                }
            }
        }.addOnFailureListener {
            when (activity) {
                is UserProfileActivity -> {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar(it.message.toString(), true)
                }
                is AddProductActivity -> {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar(it.message.toString(), true)
                }
            }


        }
    }

    fun uploadProductDetails(activity: AddProductActivity, productInfo: Product) {

        // The "users" is collection name. If the collection is already created then it will not create the same one again.
        mFireStore.collection(Constants.PRODUCTS)
            // Document ID for users fields. Here the document it is the User ID.
            .document()
            // Here the userInfo are Field and the SetOption is set to merge. It is for if we wants to merge later on instead of replacing the fields.
            .set(productInfo, SetOptions.merge())
            .addOnSuccessListener {

                // Here call a function of base activity for transferring the result to it.
                activity.productUploadSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error while uploading the product details.",
                    e
                )
            }
    }

    fun getProductsList(fragment: Fragment) {
        mFireStore.collection(Constants.PRODUCTS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                val productsList: ArrayList<Product> = ArrayList()

                for (i in document.documents) {

                    val product = i.toObject(Product::class.java)
                    product!!.id = i.id

                    productsList.add(product)
                }

                when (fragment) {
                    is ProductsFragment -> {
                        fragment.successProductsListFromFireStore(productsList)
                    }

                }
            }.addOnFailureListener {
                when (fragment) {
                    is ProductsFragment -> {
                        fragment.hideProgressDialog()
                        fragment.showErrorSnackBar(it.message.toString(), true)
                    }
                }
            }
    }

    fun getDashBoardItemsList(fragment: DashboardFragment) {
        mFireStore.collection(Constants.PRODUCTS)
            .get()
            .addOnSuccessListener { document ->
                val productsList: ArrayList<Product> = ArrayList()

                for (i in document.documents) {
                    val product = i.toObject(Product::class.java)
                    if (product != null) {
                        product.id = i.id

                        productsList.add(product)
                    }


                    fragment.successDashboardItemsList(productsList)
                }
            }.addOnFailureListener {
                fragment.hideProgressDialog()
                fragment.showErrorSnackBar(it.message.toString(), true)
            }
    }

    fun deleteProduct(fragment: ProductsFragment, productID: String) {
        mFireStore.collection(Constants.PRODUCTS)
            .document(productID)
            .delete()
            .addOnSuccessListener {

                fragment.productDeleteSuccess()

            }.addOnFailureListener {
                fragment.hideProgressDialog()
                fragment.showErrorSnackBar(it.message.toString(), true)

            }
    }

    fun getProductDetails(activity: ProductDetailsActivity, productId: String) {
        mFireStore.collection(Constants.PRODUCTS)
            .document(productId)
            .get()
            .addOnSuccessListener { document ->
                val product = document.toObject<Product>()
                if (product != null) {
                    product.id = document.id
                    activity.productDetailsSuccess(product)
                }
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                activity.showErrorSnackBar(it.message.toString(), true)
            }
    }

    fun addCartItems(activity: ProductDetailsActivity, addToCart: CartItem) {
        mFireStore.collection(Constants.CART_ITEMS)
            .document()
            .set(addToCart, SetOptions.merge())
            .addOnSuccessListener {
                activity.addToCartSuccess()
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                activity.showErrorSnackBar(it.message.toString(), true)
            }
    }

    fun checkIfItemExistInCart(activity: ProductDetailsActivity, productId: String) {
        mFireStore.collection(Constants.CART_ITEMS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserID())
            .whereEqualTo(Constants.PRODUCT_ID, productId)
            .get()
            .addOnSuccessListener { document ->
                if (document.documents.size > 0) {
                    activity.productExistsInCart()
                } else {
                    activity.productNotInCart()
                }
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                activity.showErrorSnackBar(it.message.toString(), true)
            }
    }

    fun getCartList(activity: Activity) {
        mFireStore.collection(Constants.CART_ITEMS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                val cartList = ArrayList<CartItem>()
                for (i in document.documents) {
                    val cartItem = i.toObject(CartItem::class.java)!!
                    cartItem.id = i.id
                    cartList.add(cartItem)
                }
                when (activity) {
                    is CartListActivity -> {
                        activity.successCartItemsList(cartList)
                    }
                    is CheckoutActivity -> {
                        activity.successCartItemsList(cartList)
                    }

                }

            }
            .addOnFailureListener {
                when (activity) {
                    is CartListActivity -> {
                        activity.hideProgressDialog()
                        activity.showErrorSnackBar(it.message.toString(), true)
                    }

                }
            }
    }

    fun updateMyCart(context: Context, cartId: String, itemHashMap: HashMap<String, Any>) {
        mFireStore.collection(Constants.CART_ITEMS)
            .document(cartId)
            .update(itemHashMap)
            .addOnSuccessListener {
                when (context) {
                    is CartListActivity -> {
                        context.successUpdatedItem()
                    }
                }
            }
            .addOnFailureListener {
                when (context) {
                    is CartListActivity -> {
                        context.hideProgressDialog()
                        context.showErrorSnackBar(it.message.toString(), true)
                    }
                }
            }
    }

    fun deleteItemFromCart(context: Context, cart_id: String) {
        mFireStore.collection(Constants.CART_ITEMS)
            .document(cart_id)
            .delete()
            .addOnSuccessListener {
                when (context) {
                    is CartListActivity -> {
                        context.successDeletedItem()
                    }
                }

            }
            .addOnFailureListener {
                when (context) {
                    is CartListActivity -> {
                        context.hideProgressDialog()
                        context.showErrorSnackBar(it.message.toString(), true)
                    }
                }
            }
    }

    fun getAllProductsList(activity: Activity) {
        mFireStore.collection(Constants.PRODUCTS)
            .get()
            .addOnSuccessListener { document ->
                val productsList = ArrayList<Product>()

                for (i in document.documents) {
                    val product = i.toObject(Product::class.java)!!

                    product.id = i.id
                    productsList.add(product)
                }
                when (activity) {
                    is CartListActivity -> {
                        activity.successProductsListFromFireStore(productsList)
                    }
                    is CheckoutActivity -> {
                        activity.successProductsListFromFireStore(productsList)
                    }
                }

            }
            .addOnFailureListener {
                when (activity) {
                    is CartListActivity -> {
                        activity.hideProgressDialog()
                        activity.showErrorSnackBar(it.message.toString(), true)
                    }
                    is CheckoutActivity -> {
                        activity.hideProgressDialog()
                        activity.showErrorSnackBar(it.message.toString(), true)
                    }
                }
            }
    }

    fun addAddress(activity: AddEditAddressActivity, addressModel: Address) {
        mFireStore.collection(Constants.ADDRESSES)
            .document()
            .set(addressModel, SetOptions.merge())
            .addOnSuccessListener {
                activity.successAddressAddUpdate()
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                activity.showErrorSnackBar(it.message.toString(), true)
            }

    }

    fun getAddressesList(activity: AddressListActivity) {
        mFireStore.collection(Constants.ADDRESSES)
            .whereEqualTo(Constants.USER_ID, getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                val addressList: ArrayList<Address> = ArrayList()

                for (i in document.documents) {
                    val address: Address = i.toObject(Address::class.java)!!
                    address.id = i.id
                    addressList.add(address)
                }
                activity.successAddressListFromFirestore(addressList)
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                activity.showErrorSnackBar(it.message.toString(), true)
            }
    }

    fun updateAddress(activity: AddEditAddressActivity, addressInfo: Address, addressId: String) {
        mFireStore.collection(Constants.ADDRESSES)
            .document(addressId)
            .set(addressInfo, SetOptions.merge())
            .addOnSuccessListener {
                activity.successAddressAddUpdate()
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                activity.showErrorSnackBar(it.message.toString(), true)
            }
    }

    fun deleteAddress(activity: AddressListActivity, addressId: String) {
        mFireStore.collection(Constants.ADDRESSES)
            .document(addressId)
            .delete()
            .addOnSuccessListener {
                activity.successAddressDelete()
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                activity.showErrorSnackBar(it.message.toString(), true)
            }
    }

    fun placeOrder(activity: CheckoutActivity, order: Order) {
        mFireStore.collection(Constants.ORDERS)
            .document()
            .set(order, SetOptions.merge())
            .addOnSuccessListener {
                activity.successOrderPlaced()
            }
            .addOnFailureListener {
                activity.hideProgressDialog()
                activity.showErrorSnackBar(it.message.toString(), true)
            }
    }

    fun updateAllDetails(activity: CheckoutActivity, cartList: ArrayList<CartItem>, order: Order) {
        val writeBatch = mFireStore.batch()

        for (cartItem in cartList) {
//            val productHashMap = HashMap<String, Any>()
//
//            productHashMap[Constants.STOCK_QUANTITY] =
//                (cartItem.stock_quantity.toInt() - cartItem.cart_quantity.toInt()).toString()

            val soldProduct = SoldProduct(
                cartItem.product_owner_id,
                cartItem.title,
                cartItem.price,
                cartItem.cart_quantity,
                cartItem.image,
                order.title,
                order.order_datetime,
                order.sub_total_amount,
                order.shipping_charge,
                order.total_amount,
                order.address
            )

            val documentReference =
                mFireStore.collection(Constants.SOLD_PRODUCTS).document()

            writeBatch.set(documentReference, soldProduct)
        }

        for (cartItem in cartList) {
            val documentReference =
                mFireStore.collection(Constants.CART_ITEMS).document(cartItem.id)
            writeBatch.delete(documentReference)
        }

        writeBatch.commit()
            .addOnSuccessListener {
                activity.successfullyUpdatedAllDetails()
            }.addOnFailureListener {
                activity.hideProgressDialog()
                activity.showErrorSnackBar(it.message.toString(), true)
            }
    }

    fun getMyOrdersList(fragment: OrdersFragment) {
        mFireStore.collection(Constants.ORDERS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                val ordersList = ArrayList<Order>()

                for (i in document.documents) {
                    val orderItem = i.toObject(Order::class.java)!!
                    orderItem.id = i.id
                    ordersList.add(orderItem)
                }

                fragment.populateOrdersListInUI(ordersList)

            }
            .addOnFailureListener {
                fragment.hideProgressDialog()
                fragment.showErrorSnackBar(it.message.toString(), true)
            }
    }

    fun getSoldProductsList(fragment: SoldProductsFragment) {
        mFireStore.collection(Constants.SOLD_PRODUCTS)
            .whereEqualTo(Constants.USER_ID, getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                val list = ArrayList<SoldProduct>()

                for(i in document.documents){

                    val soldProduct = i.toObject(SoldProduct::class.java)!!
                    soldProduct.id=i.id

                    list.add(soldProduct)

                }
                fragment.successSoldProductsList(list)
            }
            .addOnFailureListener {
                fragment.hideProgressDialog()
                fragment.showErrorSnackBar(it.message.toString(), true)
            }
    }


}