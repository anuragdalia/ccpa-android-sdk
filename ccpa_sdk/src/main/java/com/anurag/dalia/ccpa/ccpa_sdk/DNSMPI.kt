package com.anurag.dalia.ccpa.ccpa_sdk

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.edit
import com.anurag.dalia.ccpa.ccpa_sdk.Utils.getCountry
import java.lang.ref.WeakReference


class DNSMPI @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val ccpaSellDataKey = "ccpa_sell_data"
        private const val isCaliforniaOrUnknownKey = "is_california_or_unknown"

        private var testingFlag = 0
        private var isTesting = false

        private var views = ArrayList<WeakReference<DNSMPI>>()
        private fun getSharedPrefs(context: Context): SharedPreferences = context.getSharedPreferences("ccpa_sdk_27863", MODE_PRIVATE)


        fun enableTesting(enable: Boolean) = run {
            isTesting = enable
            views.forEach { it.get()?.addLink(false) }
        }

        fun forceInCalifornia() = run {
            testingFlag = 1
            views.forEach { it.get()?.addLink(false) }
        }

        fun forceUnknown() = run {
            testingFlag = 0
            views.forEach { it.get()?.addLink(false) }
        }

        fun forceOutsideCalifornia() = run {
            testingFlag = 2
            views.forEach { it.get()?.addLink(false) }
        }

        fun isCaliforniaOrUnknown(context: Context): Boolean = getSharedPrefs(context).getBoolean(isCaliforniaOrUnknownKey, true)

        fun canSellData(context: Context): Boolean = getSharedPrefs(context).getBoolean(ccpaSellDataKey, true)

        fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
            if (requestCode == 333) {
                views.forEach { it.get()?.addLink(false) }
            }
        }

        fun updateActivitySharedPref(activity: Activity) {
            val sharedPref: SharedPreferences = activity.getPreferences(MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putInt("gad_rdp", if (canSellData(activity)) 1 else 0)
            editor.apply()
        }
    }

    private val dnsmpiCustomLayout: Int
    private val dnsmpiLinkColor: Int
    private val dnsmpiTwoStates: Boolean
    private val dnsmpiAskGeoPermission: Boolean
    private val dnsmpiStateAText: String
    private val dnsmpiStateBText: String

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.DNSMPI, defStyleAttr, 0)
        dnsmpiCustomLayout = a.getResourceId(R.styleable.DNSMPI_dnsmpi_custom_layout, R.layout.dnsmpi_popup_simple_layout)
        dnsmpiLinkColor = a.getColor(R.styleable.DNSMPI_dnsmpi_link_color, Color.BLACK)
        dnsmpiTwoStates = a.getBoolean(R.styleable.DNSMPI_dnsmpi_two_states, true)
        dnsmpiAskGeoPermission = a.getBoolean(R.styleable.DNSMPI_dnsmpi_ask_geo_permission, true)
        dnsmpiStateAText = a.getString(R.styleable.DNSMPI_dnsmpi_state_a_text) ?: "Are you from California?"
        dnsmpiStateBText = a.getString(R.styleable.DNSMPI_dnsmpi_state_b_text) ?: "To show you personalised advertisements. We share your data with Advertisers in exchange for some money to be able to provide this app for free. Are you okay with this?"
        a.recycle()

        addLink(dnsmpiAskGeoPermission)

        views.add(WeakReference(this))
    }

    fun addLink(askPerms: Boolean) {
        val mDNSMPILink = AppCompatTextView(context).apply {
            text = "Do not sell my personal information"
            textSize = 12f
            setTextColor(dnsmpiLinkColor)
            setOnClickListener(this@DNSMPI::onLinkClicked)
        }
        val activity = getActivity()
        val country = getCountry(activity, askPerms)

        removeAllViews()
        val countryIsCaliforniaOrUnknown =
            when (isTesting) {
                true -> when (testingFlag) {
                    0 -> true
                    1 -> true
                    2 -> false
                    else -> false
                }
                false -> country == null || country.contains("california", true)
            }

        getSharedPrefs(context).edit(true) { putBoolean(isCaliforniaOrUnknownKey, countryIsCaliforniaOrUnknown) }

        if (countryIsCaliforniaOrUnknown)
            addView(mDNSMPILink, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
    }

    private fun onLinkClicked(v: View) {
        val alertDialog = AlertDialog.Builder(context)
            .setCancelable(true)
            .setView(this.dnsmpiCustomLayout)
            .create()
        alertDialog.show()

        val stateA = alertDialog.findViewById<LinearLayoutCompat>(R.id.state_a)
        val stateB = alertDialog.findViewById<LinearLayoutCompat>(R.id.state_b)
        val ccpaEligibleQuestion = alertDialog.findViewById<AppCompatTextView>(R.id.ccpa_eligible_question)
        val okayToSellQuestion = alertDialog.findViewById<AppCompatTextView>(R.id.okay_to_sell_question)
        val yesCcpaEligible = alertDialog.findViewById<AppCompatTextView>(R.id.yes_ccpa_eligible)
        val noCcpaEligible = alertDialog.findViewById<AppCompatTextView>(R.id.no_ccpa_eligible)
        val yesOkayToSell = alertDialog.findViewById<AppCompatTextView>(R.id.yes_okay_to_sell)
        val notOkayToSell = alertDialog.findViewById<AppCompatTextView>(R.id.not_okay_to_sell)

        ccpaEligibleQuestion?.text = dnsmpiStateAText
        okayToSellQuestion?.text = dnsmpiStateBText

        if (dnsmpiTwoStates) {
            stateA?.visibility = View.VISIBLE
            stateB?.visibility = View.GONE
        } else {
            stateA?.visibility = View.GONE
            stateB?.visibility = View.VISIBLE
        }

        yesCcpaEligible?.setOnClickListener {
            stateA?.visibility = View.GONE
            stateB?.visibility = View.VISIBLE
        }

        noCcpaEligible?.setOnClickListener {
            setPref(it.context.applicationContext, true)
            alertDialog.dismiss()
        }

        yesOkayToSell?.setOnClickListener {
            setPref(it.context.applicationContext, true)
            alertDialog.dismiss()
        }

        notOkayToSell?.setOnClickListener {
            setPref(it.context.applicationContext, false)
            alertDialog.cancel()
        }
    }

    private fun getActivity(): Activity? {
        var context = context
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }

    private fun setPref(context: Context, b: Boolean) {
        getSharedPrefs(context).edit(true) { putBoolean(ccpaSellDataKey, b) }
    }
}