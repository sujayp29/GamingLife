package com.sleepysoft.gaminglife.activities

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.sleepysoft.gaminglife.R
import glcore.GlEncryption
import glcore.GlLog


class GeneratePairActivity : AppCompatActivity() {
    var mPrevPoW = 0
    var mExpectPoW = 0
    val mQuitFlag = mutableListOf(false)
    val mGlEncryption = GlEncryption()
    var mCalculateGlIdThread = CalculateGlIdThread(8, mGlEncryption, mQuitFlag)

    private var mHandler = Handler(Looper.getMainLooper())
    private val mRunnable = Runnable { updateKeyGenInfo() }

    lateinit var mSeekBarPoW: SeekBar
    lateinit var mTextOutput: TextView
    lateinit var mTextSeekPow: TextView
    lateinit var mButtonGenerate: Button

    class CalculateGlIdThread(
        var pow: Int,
        val mGlEncryption: GlEncryption,
        val quitFlag: List< Boolean >) : Thread("CalculateGlIdThread") {

        @RequiresApi(Build.VERSION_CODES.O)
        override fun run() = mGlEncryption.createKeyPair(pow, quitFlag).let {  }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_key_pair)

        mTextSeekPow = findViewById(R.id.id_text_current_pow)

        mSeekBarPoW = findViewById(R.id.id_seek_pow)
        mSeekBarPoW.run {
            min = 8
            max = 32
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (progress < 10) {
                        mTextSeekPow.text = " %d".format(progress)
                    } else {
                        mTextSeekPow.text = "%d".format(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {

                }
            })
        }

        mTextOutput = findViewById(R.id.id_text_output)
        // https://www.geeksforgeeks.org/how-to-make-textview-scrollable-in-android/
        mTextOutput.movementMethod = ScrollingMovementMethod()

        mButtonGenerate = findViewById(R.id.id_button_generate_glid)
        mButtonGenerate.setOnClickListener {
            if (!mCalculateGlIdThread.isAlive) {
                mQuitFlag[0] = false
                mPrevPoW = 0
                mExpectPoW = mSeekBarPoW.progress

                mCalculateGlIdThread = CalculateGlIdThread(
                    mExpectPoW, mGlEncryption, mQuitFlag).apply { start() }

                mButtonGenerate.isEnabled = false
                mHandler.postDelayed(mRunnable, 100)
            }
        }

        val buttonCancelGenerate: Button = findViewById< Button >(R.id.id_button_cancel_generate)
        buttonCancelGenerate.setOnClickListener {
            if (mCalculateGlIdThread.isAlive) {
                mQuitFlag[0] = true
            }
        }
    }

    fun updateKeyGenInfo() {
        if (mPrevPoW != mGlEncryption.keyPairPow) {
            mPrevPoW = mGlEncryption.keyPairPow
            val text = "Loop: %d -> Max PoW: %d\n".format(
                mGlEncryption.powLoop, mGlEncryption.keyPairPow)
            mTextOutput.append(text)
        }

        if (mCalculateGlIdThread.isAlive) {
            mHandler.postDelayed(mRunnable, 100)
        } else {
            mButtonGenerate.isEnabled = true

            if (mPrevPoW >= mExpectPoW) {
                // Got the expect PoW
            }
        }
    }
}