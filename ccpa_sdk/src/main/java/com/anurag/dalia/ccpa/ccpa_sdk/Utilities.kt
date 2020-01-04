package com.anurag.dalia.ccpa.ccpa_sdk

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.telephony.TelephonyManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*

object Utils {
    val PERMISSIONS = arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)

    @SuppressLint("MissingPermission")
    fun getCountry(context: Activity?, askPerms: Boolean): String? {
        try {
            val country: String?
            if (context == null) return null

            val hasPermCoarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            val hasPermFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)

            if (PackageManager.PERMISSION_GRANTED == hasPermCoarseLocation && PackageManager.PERMISSION_GRANTED == hasPermFineLocation) {
                val locationManager: LocationManager? = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (locationManager != null) {
                    var location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (location == null)
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    if (location != null) {
                        val gcd = Geocoder(context, Locale.getDefault())
                        val addresses: List<Address>?
                        addresses = gcd.getFromLocation(location.latitude, location.longitude, 1)
                        if (addresses != null && addresses.isNotEmpty()) {
                            country = "${addresses[0].adminArea} ${addresses[0].countryName}"
                            return country
                        }
                    }
                }
            } else {
                if (askPerms)
                    ActivityCompat.requestPermissions(context, PERMISSIONS, 333)
            }
        } catch (e: Exception) {
            return null
        }
        return null
    }

    /**
     * Get ISO 3166-1 alpha-2 country code for this device (or null if not available)
     *
     * @param context Context reference to get the TelephonyManager instance from
     * @return country code or null
     */
    private fun getCountryBasedOnSimCardOrNetwork(context: Context): String? {
        try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val simCountry = tm.simCountryIso
            if (simCountry != null && simCountry.length == 2) return simCountry.toLowerCase(Locale.US)
            else if (tm.phoneType != TelephonyManager.PHONE_TYPE_CDMA) {
                val networkCountry = tm.networkCountryIso
                if (networkCountry != null && networkCountry.length == 2) return networkCountry.toLowerCase(Locale.US)
            }
        } catch (e: Exception) {

        }
        return null
    }
}