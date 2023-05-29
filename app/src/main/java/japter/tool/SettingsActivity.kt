package japter.tool

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.htmlEncode
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.work.WorkInfo
import androidx.work.WorkManager

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            this.findPreference<Preference>("history")
                ?.setOnPreferenceClickListener(fun(it: Preference): Boolean {
                    val html = StringBuilder()
                    html.append("Notifications Message History<ul>")
                    for (item in (it.sharedPreferences.getString("history", "-") ?: "").split("\n")) {
                        html.append("<li><code>").append(item.htmlEncode()).append("</code></li>")
                    }
                    html.append("</ul>")
                    WebViewActivity.show(this.requireContext(), html.toString())
                    return true
                })


            this.findPreference<Preference>("eftSumStatus")
                ?.setOnPreferenceClickListener(fun(it: Preference): Boolean {
                    val workManager = WorkManager.getInstance(this.requireContext())
                    val workInfos = workManager.getWorkInfosByTag("eft").get()
                    var enqueued = 0
                    var failed = 0
                    var running = 0
                    var succeeded = 0
                    var others = 0
                    for (item in workInfos) {
                        when (item.state) {
                            WorkInfo.State.ENQUEUED -> enqueued += 1
                            WorkInfo.State.FAILED -> failed += 1
                            WorkInfo.State.RUNNING -> running += 1
                            WorkInfo.State.SUCCEEDED -> succeeded += 1
                            else -> others += 1
                        }
                    }
                    val html = StringBuilder()
                    html.append("Enqueued = $enqueued<br>")
                    html.append("Failed = $failed<br>")
                    html.append("Running = $running<br>")
                    html.append("Succeeded = $succeeded<br>")
                    html.append("Others = $others<br>")
                    WebViewActivity.show(this.requireContext(), html.toString())
                    return true
                })

            this.findPreference<Preference>("eftQueueCancel")
                ?.setOnPreferenceClickListener(fun(it: Preference): Boolean {
                    val workManager = WorkManager.getInstance(this.requireContext())
                    val workInfos = workManager.getWorkInfosByTag("eft").get()
                    val html = StringBuilder()
                    html.append("Canceled<ul>")
                    for (item in workInfos) {
                        html.append("<li><code>").append(item.id.toString().htmlEncode()).append("</code></li>")
                    }
                    html.append("</ul>")
                    workManager.cancelAllWorkByTag("eft").result.get()
                    workManager.pruneWork().result.get()
                    WebViewActivity.show(this.requireContext(), html.toString())
                    return true
                })
        }
    }
}