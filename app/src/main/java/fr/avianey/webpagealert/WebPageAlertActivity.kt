package fr.avianey.webpagealert

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

class WebPageAlertActivity : AppCompatActivity() {

    companion object {
        const val APP_TAG = "WebPageAlert"
        const val KEY_URL = "url"
        const val KEY_FREQUENCY = "frequency"
        const val KEY_NOW = "now"

        private const val CRAWL_FREQUENCY_MIN = 60
        private const val CRAWL_FREQUENCY_MAX = 60 * 24 // 1 day
        private const val CRAWL_FREQUENCY_DEFAULT = CRAWL_FREQUENCY_MIN
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
                // TODO crawl now
                true
            }
        }

        override fun onResume() {
            super.onResume()
            PreferenceManager.getDefaultSharedPreferences(requireContext()).let {
                it.registerOnSharedPreferenceChangeListener(this)
                onSharedPreferenceChanged(it, KEY_URL)
            }
        }

        override fun onPause() {
            super.onPause()
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                    .unregisterOnSharedPreferenceChangeListener(this)
            if (frequencyChanged) {
                // TODO reschedule
            }
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            when (key) {
                KEY_URL -> {
                    // url changed
                    val url = sharedPreferences!!.getString(key, "")
                    findPreference<Preference>(key)?.summary = url
                    Log.d(APP_TAG, "URL changed to $url")
                }
                KEY_FREQUENCY -> {
                    frequencyChanged = true
                }
            }
        }

    }

}