package fr.avianey.webpagealert

import android.app.Application
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.NoCache


class WebPageAlertApplication: Application() {

    internal lateinit var requestQueue: RequestQueue

    override fun onCreate() {
        requestQueue = RequestQueue(NoCache(), BasicNetwork(HurlStack()))
        requestQueue.start()
        super.onCreate()
    }

}