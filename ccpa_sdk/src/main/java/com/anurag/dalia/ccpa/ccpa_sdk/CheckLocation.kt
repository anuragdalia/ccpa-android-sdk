package com.anurag.dalia.ccpa.ccpa_sdk

import android.os.AsyncTask
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class DetermineLocation(private val onCountryFiguredOutListener: OnCountryFiguredOutListener) : AsyncTask<Void, Void, String?>() {

    override fun doInBackground(vararg params: Void): String? {
        val mUrl = URL("https://api.videous.io/api/utils/me/location")//http://ip-api.com/line/?fields=49359
        val httpConnection: HttpsURLConnection = mUrl.openConnection() as HttpsURLConnection
        try {
            httpConnection.requestMethod = "GET"
            httpConnection.setRequestProperty("Content-length", "0")
            httpConnection.useCaches = true
            httpConnection.allowUserInteraction = false
            httpConnection.connectTimeout = 10000
            httpConnection.readTimeout = 10000
            httpConnection.connect()
            val responseCode: Int = httpConnection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val br = BufferedReader(InputStreamReader(httpConnection.inputStream))
                val sb = StringBuilder("")
                var line = ""
                while (br.readLine()?.also { line = it } != null) {
                    sb.append("$line\n")
                }
                br.close()
                val resp = sb.toString()
                return if (resp.contains("req-success")) resp else null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            onCountryFiguredOutListener.onError()
        } catch (ex: Exception) {
            ex.printStackTrace()
            onCountryFiguredOutListener.onError()
        } finally {
            httpConnection.disconnect()
        }


        return null
    }

    override fun onPostExecute(s: String?) {
        super.onPostExecute(s)
        onCountryFiguredOutListener.onCountryFiguredOut(s)
    }
}

interface OnCountryFiguredOutListener {
    fun onCountryFiguredOut(country: String?)
    fun onError()
}