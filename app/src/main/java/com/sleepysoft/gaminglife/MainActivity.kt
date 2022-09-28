package com.sleepysoft.gaminglife

import Example.ExampleView
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import glcore.GlRoot
import graphengine.GraphView

class MainActivity : AppCompatActivity() {

    private lateinit var mView: GraphView
    private lateinit var mController: GlTimeViewController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GlRoot.init()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1)
        {
            setShowWhenLocked(true)
        }
        else
        {
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }

        mView = GraphView(this)
        mController = GlTimeViewController(mView, GlRoot.glData).apply {
            this.init()
            mView.setObserver(this)
        }

        setContentView(mView)
    }
}