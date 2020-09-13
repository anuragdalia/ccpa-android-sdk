package com.anurag.dalia.ccpa.ccpa_sdk

interface OnCountryFiguredOutListener {
    fun onCountryFiguredOut(country: String?)
    fun onError()
}