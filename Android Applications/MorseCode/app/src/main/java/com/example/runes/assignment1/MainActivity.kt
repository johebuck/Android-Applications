package com.example.runes.assignment1

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.preference.PreferenceManager.getDefaultSharedPreferences
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.runes.assignment1.R.id.mTextView

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.timerTask

val SAMPLE_RATE = 44100

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var letToCodeDict: HashMap<String, String> = HashMap()
    private var codeToLetDict: HashMap<String, String> = HashMap()

    var prefs: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getDefaultSharedPreferences(this.applicationContext)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        testButton.setOnClickListener { _ ->
            mTextView.text = ""
            appendTextAndScroll(inputText.text.toString().toUpperCase())
            hideKeyboard()
        }

        mTextView.movementMethod = ScrollingMovementMethod()

        val jsonObj = loadMorseJSON()
        buildDictsWithJSON(jsonObj)

        cButton.setOnClickListener { _ ->
            mTextView.text = ""
            showCodes()
            hideKeyboard()
        }

        tButton.setOnClickListener { _ ->
            mTextView.text = ""

            val input = inputText.text.toString()

            appendTextAndScroll(input.toUpperCase())

            if (input.matches("(\\.|-|\\s/\\s|\\s)+".toRegex())) {
                val transMorse = letToCode(input)
                appendTextAndScroll(transMorse.toUpperCase())
            } else {
                val Text = codeToLet(input)
                appendTextAndScroll(Text)
            }
            hideKeyboard()
        }

        playButton.setOnClickListener { _ ->
            val input = inputText.text.toString()
            playString(codeToLet(input), 0)
        }


    }

    fun Activity.hideKeyboard() {
        hideKeyboard(if (currentFocus == null) View(this) else currentFocus)
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun playString(s: String, i: Int = 0): Unit {
        if (i > s.length - 1)
            return;
        var mDelay: Long = 0;

        var thenFun: () -> Unit = { ->
            this@MainActivity.runOnUiThread(java.lang.Runnable { playString(s, i + 1) })
        }

        var c = s[i]
        Log.d("Log", "Processing pos: " + i + " char: [" + c + "]")
        if (c == '.')
            playDot(thenFun)
        else if (c == '-')
            playDash(thenFun)
        else if (c == '/')
            pause(6 * dotLength, thenFun)
        else if (c == ' ')
            pause(2 * dotLength, thenFun)
    }

    val dotLength: Int = 50
    val dashLength: Int = dotLength * 3

    //val morsePitch = prefs!!.getString("morse_pitch", "550").toInt()
    // Put in oncreate and set these to null.
    val dotSoundBuffer: ShortArray = genSineWaveSoundBuffer(550.0, dotLength) //freq: 550.0
    val dashSoundBuffer: ShortArray = genSineWaveSoundBuffer(550.0, dashLength)

    fun playDash(onDone: () -> Unit = {}) {
        Log.d("DEBUG", "playDash")
        playSoundBuffer(dashSoundBuffer, { -> pause(dotLength, onDone) })
    }

    fun playDot(onDone: () -> Unit = {}) {
        Log.d("DEBUG", "playDot")
        playSoundBuffer(dotSoundBuffer, { -> pause(dotLength, onDone) })
    }

    fun pause(durationMSec: Int, onDone: () -> Unit = {}) {
        Log.d("DEBUG", "pause: ${durationMSec}")
        Timer().schedule(timerTask { onDone() }, durationMSec.toLong())
    }

    private fun genSineWaveSoundBuffer(frequency: Double, durationMSec: Int): ShortArray {
        val duration: Int = Math.round((durationMSec / 1000.0) * SAMPLE_RATE).toInt()

        var mSound: Double
        val mBuffer = ShortArray(duration)
        for (i in 0 until duration) {
            mSound = Math.sin(2.0 * Math.PI * i.toDouble() / (SAMPLE_RATE / frequency))
            mBuffer[i] = (mSound * java.lang.Short.MAX_VALUE).toShort()
        }
        return mBuffer
    }

    private fun playSoundBuffer(mBuffer: ShortArray, onDone: () -> Unit = { }) {
        var minBufferSize = SAMPLE_RATE / 10
        if (minBufferSize < mBuffer.size) {
            minBufferSize = minBufferSize + minBufferSize *
                    (Math.round(mBuffer.size.toFloat()) / minBufferSize.toFloat()).toInt()
        }

        val nBuffer = ShortArray(minBufferSize)
        for (i in nBuffer.indices) {
            if (i < mBuffer.size)
                nBuffer[i] = mBuffer[i]
            else
                nBuffer[i] = 0
        }

        val mAudioTrack = AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize, AudioTrack.MODE_STREAM)

        mAudioTrack.setStereoVolume(AudioTrack.getMaxVolume(), AudioTrack.getMaxVolume())
        mAudioTrack.setNotificationMarkerPosition(mBuffer.size)
        mAudioTrack.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
            override fun onPeriodicNotification(track: AudioTrack) {}
            override fun onMarkerReached(track: AudioTrack?) {
                Log.d("Log", "Audio track end of file reached...")
                mAudioTrack.stop()
                mAudioTrack.release()
                onDone()
            }
        })
        mAudioTrack.play()
        mAudioTrack.write(nBuffer, 0, minBufferSize)
    }

    private fun letToCode(input: String) : String {
        var r = ""
        val s = input.split("(\\s)+".toRegex())
        for (c in s) {
            if (c == "/")
                r += " "
            else if (codeToLetDict.containsKey(c))
                r += codeToLetDict[c]
            else
                r += "[NA]"
        }

        Log.d("log", "Text: $r")

        return r
    }

    private fun codeToLet(input : String) : String {
        var r = ""
        val s = input.toLowerCase()
        for (c in s) {
            if (c == ' ')
                r += "/ "
            else if (letToCodeDict.containsKey(c.toString()))
                r += "${letToCodeDict[c.toString()]} "
            else
                r += "? "
        }
        return r
    }

    private fun showCodes() {
        appendTextAndScroll("HERE ARE THE CODES")
        for (key in letToCodeDict.keys.sorted()){
            appendTextAndScroll("${key.toUpperCase()}: ${letToCodeDict[key]}")
        }
    }

    private fun appendTextAndScroll(text: String){
        if (mTextView != null){
            mTextView.append(text + "\n")
            val layout = mTextView.getLayout()
            if (layout != null) {
                val scrollDelta = (layout.getLineBottom(  mTextView.getLineCount() - 1)
                        - mTextView.getScrollY() - mTextView.getHeight())
                if (scrollDelta > 0)
                    mTextView.scrollBy( 0, scrollDelta)
            }
        }
    }

    private fun buildDictsWithJSON(jsonObj : JSONObject) {
        for(k in jsonObj.keys()){
            val code : String = jsonObj[k] as String
            letToCodeDict.set(k,code)
            codeToLetDict.set(code,k)
        }
    }

    private fun loadMorseJSON() : JSONObject {
        val filePath = "morse.json"
        val json_string = application.assets.open(filePath).bufferedReader().use{
            it.readText()
        }
        val jsonObj = JSONObject(json_string.substring(json_string.indexOf("{"), json_string.lastIndexOf("}") + 1))

        return jsonObj
    }

    override fun onClick(p0: View?) {
        val input = inputText.text
        mTextView.text = "$input"
        hideKeyboard()
    }

    //FOUND AT: http://programminget.blogspot.com/2017/08/how-to-close-android-soft-keyboard.html
    private fun hideKeyboard() {
        val inputManager:InputMethodManager =getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(currentFocus.windowToken, InputMethodManager.SHOW_FORCED)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
