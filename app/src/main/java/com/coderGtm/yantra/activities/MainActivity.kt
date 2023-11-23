package com.coderGtm.yantra.activities

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.view.KeyEvent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.coderGtm.yantra.ActivityRequestCodes
import com.coderGtm.yantra.R
import com.coderGtm.yantra.SHARED_PREFS_FILE_NAME
import com.coderGtm.yantra.YantraLauncher
import com.coderGtm.yantra.databinding.ActivityMainBinding
import com.coderGtm.yantra.getInit
import com.coderGtm.yantra.isNetworkAvailable
import com.coderGtm.yantra.requestCmdInputFocusAndShowKeyboard
import com.coderGtm.yantra.requestUpdateIfAvailable
import com.coderGtm.yantra.runInitTasks
import com.coderGtm.yantra.setWallpaperFromUri
import com.coderGtm.yantra.terminal.Terminal
import com.coderGtm.yantra.toast
import com.coderGtm.yantra.verifyValidSignature
import com.coderGtm.yantra.views.TerminalGestureListenerCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Locale


class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener, TerminalGestureListenerCallback {

    private lateinit var primaryTerminal: Terminal
    private lateinit var app: YantraLauncher
    private lateinit var binding: ActivityMainBinding
    private lateinit var billingClient: BillingClient

    var tts: TextToSpeech? = null
    var ttsTxt = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app = application as YantraLauncher
        app.preferenceObject = applicationContext.getSharedPreferences(SHARED_PREFS_FILE_NAME,0)

        primaryTerminal = Terminal(
            activity = this@MainActivity,
            binding = binding,
            preferenceObject = app.preferenceObject
        )
        primaryTerminal.initialize()
    }

    override fun onStart() {
        super.onStart()
        if (primaryTerminal.initialized) {
            Thread {
                val initList = getInit(app.preferenceObject)
                runInitTasks(initList, app.preferenceObject, primaryTerminal)
            }.start()
        }
    }
    override fun onRestart() {
        super.onRestart()
        val unwrappedCursorDrawable = AppCompatResources.getDrawable(this,
            R.drawable.cursor_drawable
        )
        val wrappedCursorDrawable = DrawableCompat.wrap(unwrappedCursorDrawable!!)
        DrawableCompat.setTint(wrappedCursorDrawable, primaryTerminal.theme.buttonColor)
        Thread {
            requestUpdateIfAvailable(app.preferenceObject, this@MainActivity)
        }.start()
    }
    override fun onDestroy() {
        super.onDestroy()
        try {
            billingClient.endConnection()
        }
        catch(_: java.lang.Exception) {}
    }
    override fun onSingleTap() {
        val oneTapKeyboardActivation = app.preferenceObject.getBoolean("oneTapKeyboardActivation",true)
        if (oneTapKeyboardActivation) {
            requestCmdInputFocusAndShowKeyboard(this@MainActivity, binding)
        }
    }
    override fun onDoubleTap() {
        val cmdToExecute = app.preferenceObject.getString("doubleTapCommand", "lock")
        if (cmdToExecute != "") {
            //execute command
            primaryTerminal.handleCommand(cmdToExecute!!)
        }
    }
    override fun onInit(status: Int) {
        //TTS Initialization function
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.getDefault())

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                primaryTerminal.output("Error: TTS language not supported!", primaryTerminal.theme.errorTextColor, null)
            } else {
                tts!!.setSpeechRate(.7f)
                tts!!.speak(ttsTxt, TextToSpeech.QUEUE_FLUSH, null,"")
            }
        }
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {
                primaryTerminal.output("TTS synthesized! Playing now...", primaryTerminal.theme.successTextColor, null)
            }
            override fun onDone(utteranceId: String) {
                primaryTerminal.output("Shutting down TTS engine...", primaryTerminal.theme.resultTextColor, null)

                if (tts != null) {
                    tts!!.stop()
                    tts!!.shutdown()
                }
                primaryTerminal.output("TTS engine shutdown.", primaryTerminal.theme.resultTextColor, null)
            }
            override fun onError(utteranceId: String) {
                primaryTerminal.output("TTS error!!", primaryTerminal.theme.errorTextColor, null)
                primaryTerminal.output("Shutting down TTS engine...", primaryTerminal.theme.resultTextColor, null)

                if (tts != null) {
                    tts!!.stop()
                    tts!!.shutdown()
                }
                primaryTerminal.output("TTS engine shutdown.", primaryTerminal.theme.resultTextColor, null)

            }
        })
    }
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_DPAD_UP && event.action == KeyEvent.ACTION_UP) {
            primaryTerminal.cmdUp()
        }
        else if (event.keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.action == KeyEvent.ACTION_UP) {
            primaryTerminal.cmdDown()
        }
        else if (event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
            val inputReceived = binding.cmdInput.text.toString().trim()
            primaryTerminal.handleInput(inputReceived)
        }
        return super.dispatchKeyEvent(event)
    }
    override fun onBackPressed() {}
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ActivityRequestCodes.IMAGE_PICK.code) {
            if (resultCode == RESULT_OK) {
                val uri = data?.data
                setWallpaperFromUri(uri, this, primaryTerminal.theme.bgColor, app.preferenceObject)
                primaryTerminal.output("Selected Wallpaper applied!", primaryTerminal.theme.successTextColor, null)
            }
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            primaryTerminal.output("Permission denied!", primaryTerminal.theme.errorTextColor, null)
        } else {
            primaryTerminal.output("Permission Granted", primaryTerminal.theme.successTextColor, null)
        }
    }

    fun initializeProductPurchase(skuId: String) {
        primaryTerminal.output("Initializing purchase...Please wait.",primaryTerminal.theme.resultTextColor, null)
        // check internet connection
        if (!isNetworkAvailable(this@MainActivity)) {
            primaryTerminal.output("No internet connection. Please connect to the internet and try again.",primaryTerminal.theme.errorTextColor, null)
            return
        }
        try {
            if (!billingClient.isReady) {
                billingClient.endConnection()
                billingClient = BillingClient.newBuilder(this)
                    .setListener(purchasesUpdatedListener)
                    .enablePendingPurchases()
                    .build()
            }
        }
        catch (e: UninitializedPropertyAccessException) {
            billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build()
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Billing client is ready, query SKU details and launch purchase flow
                    val skuList = listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(skuId)
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build())
                    val params = QueryProductDetailsParams.newBuilder()
                        .setProductList(skuList)
                        .build()

                    billingClient.queryProductDetailsAsync(params) { billingResult1, skuDetailsList ->
                        if (billingResult1.responseCode == BillingClient.BillingResponseCode.OK) {
                            // Handle retrieved SkuDetails objects here
                            val skuDetails = listOf(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(skuDetailsList[0])
                                    .build()
                            )
                            val flowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(skuDetails)
                                .build()

                            billingClient.launchBillingFlow(this@MainActivity, flowParams)
                        }
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to reconnect to the billing service
                billingClient.startConnection(this)
            }
        })
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            val isVerified = verifyValidSignature(purchase.originalJson, purchase.signature, baseContext, packageManager)
            if (!isVerified) {
                toast(baseContext, "Error : Invalid Purchase")
                return
            }
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) {
                    if (it.responseCode == BillingClient.BillingResponseCode.OK) {
                        app.preferenceObject.edit().putBoolean(JSONObject(purchase.originalJson).optString("productId")+"___purchased", true).apply()
                        runOnUiThread {
                            toast(baseContext, "Purchase successful")
                            primaryTerminal.output("[+] Purchase successful. Thank you for your support!", primaryTerminal.theme.successTextColor, null)
                        }
                    }
                }
            }
            // else already purchased and acknowledged
            else {
                // grant entitlement to the user
                app.preferenceObject.edit().putBoolean(JSONObject(purchase.originalJson).optString("productId")+"___purchased", true).apply()
            }
        }
        else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            toast(baseContext, "Purchase is Pending. Please complete Transaction")
            primaryTerminal.output("Purchase is Pending. Please complete Transaction", primaryTerminal.theme.warningTextColor, null)
        }
        else if (purchase.purchaseState == Purchase.PurchaseState.UNSPECIFIED_STATE) {
            //if purchase is refunded or unknown
            app.preferenceObject.edit().putBoolean(JSONObject(purchase.originalJson).optString("productId")+"___purchased", false).apply()
            toast(baseContext, "Purchase failed")
            primaryTerminal.output("[-] Purchase failed. Please try again.", primaryTerminal.theme.errorTextColor, null)
        }
    }
    private fun restoreAllPurchases() {
        val qpm = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        billingClient.queryPurchasesAsync(qpm) { _, p1 ->
            if (p1.isNotEmpty()) {
                for (purchase in p1) {
                    handlePurchase(purchase)
                }
            }
        }
    }



    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                }
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                toast(baseContext, "Purchase cancelled")
                primaryTerminal.output("[-] Purchase cancelled", primaryTerminal.theme.warningTextColor, null)
            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                restoreAllPurchases()
                toast(baseContext, "Already purchased")
                primaryTerminal.output("[+] This item is already purchased. Please try again.", primaryTerminal.theme.warningTextColor, null)
            } else {
                toast(baseContext, "Purchase failed")
                primaryTerminal.output("[-] Purchase failed. Please try again.", primaryTerminal.theme.errorTextColor, null)
            }
            billingClient.endConnection()
        }
    var yantraSettingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data != null) {
                val settingsChanged = data.getBooleanExtra("settingsChanged", false)
                if (settingsChanged) {
                    recreate()
                }
            }
        }
    }
}
