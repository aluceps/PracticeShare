package me.aluceps.practiceshare

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    enum class App(val packageName: String) {
        Twitter("com.twitter.android"),
        Line("jp.naver.line.android"),
    }

    private val resolveInfoFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PackageManager.MATCH_ALL
    } else {
        PackageManager.MATCH_DEFAULT_ONLY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        share.setOnClickListener { share() }
    }

    private fun share() {
        val targets: MutableList<Intent> = mutableListOf()
        val resolveInfoList = packageManager.queryIntentActivities(Intent().actionSend(), resolveInfoFlags)
        resolveInfoList.forEach {
            targets.add(Intent().actionSend().apply {
                `package` = it.activityInfo.packageName
                setClassName(it.activityInfo.packageName, it.activityInfo.name)
                putExtra(Intent.EXTRA_SUBJECT, "")
                putExtra(Intent.EXTRA_TEXT, createMessage(it.activityInfo.packageName, textView.text.toString()))
            })
        }

        if (targets.isNotEmpty()) {
            val chooserIntent = Intent.createChooser(Intent(), "共有").apply {
                putExtra(Intent.EXTRA_INITIAL_INTENTS, targets.toTypedArray())
            }
            startActivity(chooserIntent)
        }
    }

    /**
     * ハッシュタグが利用可能なアプリは
     * 共有時にハッシュタグを追加する
     */
    private fun createMessage(packageName: String, message: String): String =
            if (hashtagAble(packageName)) {
                "%s\n%s".format(message, "#some_hash_tag")
            } else {
                message
            }

    /**
     * enumで定義したパッケージ名にあるか確認
     */
    private fun hashtagAble(packageName: String): Boolean {
        var count = 0
        App.values().forEach {
            if (it.packageName.contains(packageName)) count++
        }
        return count > 0
    }

    private fun Intent.actionSend(): Intent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
    }
}
