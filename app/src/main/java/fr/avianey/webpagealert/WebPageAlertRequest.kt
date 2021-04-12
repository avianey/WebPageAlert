package fr.avianey.webpagealert

import android.util.Log
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import fr.avianey.webpagealert.WebPageAlertActivity.Companion.APP_TAG
import java.text.SimpleDateFormat
import java.util.*

class WebPageAlertRequest(
    method: Int,
    url: String,
    listener: Response.Listener<String>,
    errorListener: Response.ErrorListener,
    private val lastModifiedCallback: ((Long) -> Unit)
) : StringRequest(method, url, listener, errorListener) {

    companion object {
        private const val HEADER_LAST_MODIFIED = "Last-Modified"
        private const val HEADER_LAST_MODIFIED_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz"
        private val sdf = SimpleDateFormat(HEADER_LAST_MODIFIED_DATE_FORMAT, Locale.US)
    }

    override fun parseNetworkResponse(response: NetworkResponse?): Response<String> {
        val lastModified = response?.headers?.get(HEADER_LAST_MODIFIED)?.let {
            Log.d(APP_TAG, "Page last modified $it")
            try {
                sdf.parse(it).time
            } catch (ignore: Exception) {
                0L
            }
        } ?: 0L
        lastModifiedCallback.invoke(lastModified)
        return super.parseNetworkResponse(response)
    }

}