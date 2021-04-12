package fr.avianey.webpagealert

import android.content.Context
import androidx.startup.Initializer
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.NoCache

class WebPageAlertInitializer : Initializer<RequestQueue> {

    companion object {
        lateinit var requestQueue: RequestQueue
    }

    override fun create(context: Context): RequestQueue {
        requestQueue = RequestQueue(NoCache(), BasicNetwork(HurlStack()))
        requestQueue.start()
        return requestQueue
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

}