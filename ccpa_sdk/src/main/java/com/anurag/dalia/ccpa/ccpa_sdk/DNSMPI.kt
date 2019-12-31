package com.anurag.dalia.ccpa.ccpa_sdk

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.edit
import com.anurag.dalia.ccpa.ccpa_sdk.Utils.getCountry


class DNSMPI @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        private const val SharedPrefKey = "ccpa_sell_data"
        private var mCanSellData: Boolean? = null
        private var view: DNSMPI? = null
        private fun getSharedPrefs(context: Context): SharedPreferences = context.getSharedPreferences("ccpa_sdk_27863", MODE_PRIVATE)

        fun canSellData(context: Context): Boolean {
            if (mCanSellData == null)
                mCanSellData = getSharedPrefs(context).getBoolean(SharedPrefKey, true)

            return mCanSellData as Boolean
        }

        fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
            if (requestCode == 333) {
                view?.addLink(false)
            }
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

        view = this
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
        if (country == null || country.contains("california", true))
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
        mCanSellData = b
        getSharedPrefs(context).edit { putBoolean(SharedPrefKey, b) }
    }
}