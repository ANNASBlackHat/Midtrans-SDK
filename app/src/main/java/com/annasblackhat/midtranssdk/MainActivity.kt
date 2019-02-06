package com.annasblackhat.midtranssdk

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.midtrans.sdk.corekit.callback.TransactionFinishedCallback
import com.midtrans.sdk.corekit.core.MidtransSDK
import com.midtrans.sdk.corekit.core.PaymentMethod
import com.midtrans.sdk.corekit.core.TransactionRequest
import com.midtrans.sdk.corekit.models.ItemDetails
import com.midtrans.sdk.uikit.SdkUIFlowBuilder
import kotlinx.android.synthetic.main.activity_main.*
import com.midtrans.sdk.corekit.core.LocalDataHandler
import com.midtrans.sdk.corekit.models.UserAddress
import com.midtrans.sdk.corekit.models.UserDetail
import com.midtrans.sdk.corekit.models.snap.TransactionResult


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initMidtransSDK()
        initUserData()

        initPayAction()
    }

    private fun initPayAction() {
        val simplePayments = mapOf(bank_transfer_bca to PaymentMethod.BANK_TRANSFER_BCA)
        for((btn, payMenthod) in simplePayments) {
            btn.setOnClickListener {
                MidtransSDK.getInstance().transactionRequest = createTransactionRequest()
                MidtransSDK.getInstance().startPaymentUiFlow(this, payMenthod)
            }
        }

        midtrans_payment.setOnClickListener {
            MidtransSDK.getInstance().transactionRequest = createTransactionRequest()
            MidtransSDK.getInstance().startPaymentUiFlow(this)
        }
    }

    private fun initUserData() {
        var userDetail: UserDetail? = LocalDataHandler.readObject("user_details", UserDetail::class.java)
        if (userDetail == null) {
            userDetail = UserDetail()
            userDetail.userFullName = "Budi Utomo"
            userDetail.email = "budi@utomo.com"
            userDetail.phoneNumber = "08123456789"
            // set user ID as identifier of saved card (can be anything as long as unique),
            // randomly generated by SDK if not supplied
            userDetail.userId = "budi-6789"

            val userAddresses = ArrayList<UserAddress>()
            val userAddress = UserAddress()
            userAddress.address = "Jalan Andalas Gang Sebelah No. 1"
            userAddress.city = "Jakarta"
            userAddress.addressType = com.midtrans.sdk.corekit.core.Constants.ADDRESS_TYPE_BOTH
            userAddress.zipcode = "12345"
            userAddress.country = "IDN"
            userAddresses.add(userAddress)
            userDetail.userAddresses = userAddresses
            LocalDataHandler.saveObject("user_details", userDetail)
        }
    }

    private fun initMidtransSDK() {
        SdkUIFlowBuilder.init()
            .setClientKey(BuildConfig.CLIENT_KEY)
            .setContext(this)
            .setTransactionFinishedCallback(transactionCallBack)
            .setMerchantBaseUrl("https://www.uniq.id/")
            .enableLog(BuildConfig.DEBUG)
            .buildSDK()
    }

    private fun createTransactionRequest(): TransactionRequest {
        val transaction = TransactionRequest("ID-${System.currentTimeMillis()}", 50000.0) //Transaction ID & nominal
        transaction.itemDetails = arrayListOf(
            ItemDetails("IDPROD001", 10000.0, 3, "Product Name 1"), //IdProduct, Price, Qty, ProductName
            ItemDetails("IDPROD002", 5000.0, 4, "Product Name 2")
            )

        return transaction
    }

    private val transactionCallBack = TransactionFinishedCallback { result ->
        if(result.isTransactionCanceled){
            Toast.makeText(this@MainActivity, "Transaction Canceled", Toast.LENGTH_LONG).show()
        }else
        when(result.status){
            TransactionResult.STATUS_SUCCESS -> Toast.makeText(this@MainActivity, "Transaction Finished. ID: " + result.response.transactionId, Toast.LENGTH_LONG).show()
            TransactionResult.STATUS_PENDING -> Toast.makeText(this@MainActivity, "Transaction Pending. ID: " + result.response.transactionId, Toast.LENGTH_LONG).show()
            TransactionResult.STATUS_INVALID -> Toast.makeText(this@MainActivity, "Transaction Invalid", Toast.LENGTH_LONG).show()
            TransactionResult.STATUS_FAILED -> Toast.makeText(this@MainActivity, "Transaction Failed. ID: " + result.response.transactionId + " Message : ${result.response.statusMessage}", Toast.LENGTH_LONG).show()
            else -> Toast.makeText(this@MainActivity, "Transaction Finished with failure. ${result.status}", Toast.LENGTH_LONG).show()
        }
    }

}
