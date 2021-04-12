package fr.avianey.webpagealert

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager.getDefaultSharedPreferences
import java.text.DateFormat
import java.util.*


class WebPageAlertActivity : AppCompatActivity() {

    companion object {
        const val APP_TAG = "WebPageAlert"
        const val KEY_URL = "url"
        const val KEY_FREQUENCY = "frequency"
        const val KEY_NOW = "now"
        const val KEY_LAST_MODIFIED = "modified"
        const val KEY_LAST_CRAWLED = "crawled"

        const val CRAWL_FREQUENCY_MIN = 15
        const val CRAWL_FREQUENCY_MAX = 60 * 24 // 1 day
        const val CRAWL_FREQUENCY_DEFAULT = 60
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(android.R.id.content, SettingsFragment())
                    .commit()
        }
    }

    class SettingsFragment: PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

        private var frequencyChanged = false

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.settings)
            findPreference<Preference>(KEY_NOW)?.setOnPreferenceClickListener {
                scheduleCrawl()
                true
            }
        }

        override fun onResume() {
            super.onResume()
            getDefaultSharedPreferences(requireContext()).let {
                it.registerOnSharedPreferenceChangeListener(this)
                onSharedPreferenceChanged(it, KEY_URL)
                onSharedPreferenceChanged(it, KEY_LAST_MODIFIED)
                onSharedPreferenceChanged(it, KEY_LAST_CRAWLED)
            }
        }

        override fun onPause() {
            super.onPause()
            getDefaultSharedPreferences(requireContext())
                    .unregisterOnSharedPreferenceChangeListener(this)
            if (frequencyChanged) { scheduleCrawl() }
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            when (key) {
                KEY_URL -> {
                    // url changed
                    val url = sharedPreferences!!.getString(key, "")
                    findPreference<Preference>(key)?.summary = url
                    Log.d(APP_TAG, "URL changed to $url")
                    scheduleCrawl()
                }
                KEY_FREQUENCY -> {
                    frequencyChanged = true
                }
                KEY_LAST_MODIFIED, KEY_LAST_CRAWLED -> {
                    findPreference<Preference>(key)?.summary =
                            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM)
                                    .format(Date(sharedPreferences!!.getLong(key, 0L)))
                }
            }
        }

        private fun scheduleCrawl() {
            val url = getDefaultSharedPreferences(requireContext()).getString(KEY_URL, "")
            if (url.isNullOrBlank()) {
                WebPageAlertWorker.cancel(requireContext())
            } else {
                WebPageAlertWorker.schedule(requireContext())
            }
        }

    }

}