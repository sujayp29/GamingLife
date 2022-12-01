package graphengine

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.annotation.RequiresApi
import com.sleepysoft.gaminglife.controllers.GlControllerBuilder
import com.sleepysoft.gaminglife.controllers.GlControllerContext
import glcore.GlLog
import glcore.LONG_LONG_PRESS_TIMEOUT
import kotlin.math.abs


open class GraphItemDecorator(
    val decoratedItem: GraphItem) {

    open fun paintBeforeGraph(canvas: Canvas) {

    }
    open fun paintAfterGraph(canvas: Canvas) {

    }
}


open class GraphActionDecorator(decoratedItem: GraphItem) :
    GraphItemDecorator(decoratedItem), ActionHandler {

    override fun onActionUp(pos: PointF): ActionHandler.ACT {
        return ActionHandler.ACT.IGNORED
    }

    override fun onActionDown(pos: PointF): ActionHandler.ACT {
        return ActionHandler.ACT.IGNORED
    }

    override fun onActionMove(
        posBefore: PointF,
        posNow: PointF,
        distanceX: Float,
        distanceY: Float
    ): ActionHandler.ACT {
        return ActionHandler.ACT.IGNORED
    }

    override fun onActionFling(
        posBefore: PointF,
        posNow: PointF,
        velocityX: Float,
        velocityY: Float
    ): ActionHandler.ACT {
        return ActionHandler.ACT.IGNORED
    }

    override fun onActionClick(pos: PointF): ActionHandler.ACT {
        return ActionHandler.ACT.IGNORED
    }

    override fun onActionSelect(pos: PointF): ActionHandler.ACT {
        return ActionHandler.ACT.IGNORED
    }

    override fun onActionLongPress(pos: PointF): ActionHandler.ACT {
        return ActionHandler.ACT.IGNORED
    }

}


// ---------------------------------------------------------------------------------------------

class AutoFitTextDecorator(decoratedItem: GraphItem) : GraphItemDecorator(decoratedItem) {

    var mainText: String = ""
        set(value) {
            field = value
        }
    var fontPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    var textArea: Rect = Rect()
    var textBound: Rect = Rect()

    override fun paintAfterGraph(canvas: Canvas) {
        val newTextArea = decoratedItem.boundRect().apply { inflate(0.7f) }.toRect()
        if (textArea != newTextArea) {
            textArea = newTextArea
            textBound = decoratedItem.boundRect().apply { inflate(0.7f) }.toRect()
            val fontSize = calculateFontSize(textBound, textArea, mainText)
            fontPaint.textSize = fontSize
        }
        val halfTextHeight: Float = textBound.height() / 2.0f
        canvas.drawText(
            mainText,
            textArea.centerX().toFloat(),
            (textArea.centerY().toFloat() + halfTextHeight),
            fontPaint
        )
    }
}


// ---------------------------------------------------------------------------------------------

class ClickDecorator(
    decoratedItem: GraphItem,
    var interactiveListener: GraphInteractiveListener? = null)
    : GraphActionDecorator(decoratedItem) {

    override fun onActionClick(pos: PointF): ActionHandler.ACT {
        GlLog.i("ClickDecorator.onActionClick [$decoratedItem]")

        interactiveListener?.onItemClicked(decoratedItem)
        GlControllerBuilder.graphShadowView.invalidate()
        return ActionHandler.ACT.HANDLED
    }
}


class InteractiveDecorator(
    decoratedItem: GraphItem,
    var interactiveListener: GraphInteractiveListener? = null)
    : GraphActionDecorator(decoratedItem) {

    companion object {
        var trackingItem: GraphItem? = null
            private set

        fun changeTrackingItem(item: GraphItem) {
            trackingItem = item
        }
    }


    override fun onActionUp(pos: PointF): ActionHandler.ACT {
        if (trackingItem == decoratedItem) {
            GlLog.i("InteractiveDecorator.onActionUp [$decoratedItem]")

            decoratedItem.inflatePct = 0.0f
            interactiveListener?.onItemDropped(
                decoratedItem, intersectItems(decoratedItem))
            trackingItem = null
            GlControllerBuilder.graphShadowView.invalidate()
        }
        // Leak this action to other handler avoiding issues
        return ActionHandler.ACT.IGNORED
    }

    override fun onActionMove(posBefore: PointF, posNow: PointF,
                              distanceX: Float,distanceY: Float): ActionHandler.ACT {
        return if (decoratedItem == trackingItem) {
            GlLog.i("InteractiveDecorator.onActionMove [$decoratedItem]")

            decoratedItem.shiftItem(-distanceX, -distanceY)
            interactiveListener?.onItemDragging(
                decoratedItem, intersectItems(decoratedItem))
            GlControllerBuilder.graphShadowView.invalidate()

            ActionHandler.ACT.HANDLED
        }
        else {
            ActionHandler.ACT.IGNORED
        }
    }

/*    override fun onActionClick(pos: PointF) : ActionHandler.ACT {
        GlLog.i("InteractiveDecorator.onActionClick [$decoratedItem]")

        interactiveListener?.onItemClicked(decoratedItem)
        GlControllerBuilder.graphShadowView.invalidate()
        return ActionHandler.ACT.HANDLED
    }*/

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActionSelect(pos: PointF): ActionHandler.ACT {
        return if (decoratedItem.visible &&
                   decoratedItem.boundRect().contains(pos.x, pos.y)) {

            GlLog.i("InteractiveDecorator.onActionSelect [$decoratedItem]")

            trackingItem = decoratedItem
            decoratedItem.inflatePct = 10.0f
            decoratedItem.itemLayer?.bringGraphItemToFront(decoratedItem)
            interactiveListener?.onItemSelected(decoratedItem)

            GlControllerContext.vibrate(100)
            GlControllerContext.refresh()

            ActionHandler.ACT.HANDLED
        }
        else {
            ActionHandler.ACT.IGNORED
        }
    }

    // --------------------------------------------------------------

    private fun intersectItems(exceptItem: GraphItem?) : List< GraphItem > =
        decoratedItem.itemLayer?.itemIntersectRect(
            decoratedItem.boundRect()) { it.visible && it != exceptItem } ?: listOf()
}


class LongPressProgressDecorator(decoratedItem: GraphItem,
    val progressItem: GraphProgress,
    val abandonOffset: Float,
    var triggerListener: GraphInteractiveListener? = null)
    : GraphActionDecorator(decoratedItem) {

    var longLongPressTimeout = LONG_LONG_PRESS_TIMEOUT

    private var mPressSince: Long = 0
    private lateinit var mHandler : Handler
    private lateinit var mRunnable : Runnable

    // ---------------------------------------------------------------------

    fun init() {
        mHandler = Handler(Looper.getMainLooper())
        mRunnable = Runnable { doPeriod() }
    }

    private fun doPeriod() {
        if (progressItem.visible) {
            val duration: Int = (System.currentTimeMillis() - mPressSince).toInt()
            if (duration >= longLongPressTimeout) {
                progressItem.visible = false
                triggerListener?.onItemTriggered(decoratedItem)
            }
            else {
                progressItem.progress = duration.toFloat() / longLongPressTimeout.toFloat()
            }
            GlControllerContext.refresh()
            mHandler.postDelayed(mRunnable, 100)
        }
    }

    private fun endLongLongPress() {
        mPressSince = 0
        progressItem.visible = false
        progressItem.progress = 0.0f
    }

    // ---------------------------------------------------------------------

    override fun onActionUp(pos: PointF): ActionHandler.ACT {
        endLongLongPress()
        // Leak this action to other handler avoiding issues
        return ActionHandler.ACT.IGNORED
    }

    override fun onActionMove(posBefore: PointF, posNow: PointF,
                              distanceX: Float,distanceY: Float): ActionHandler.ACT {

        if ((abs(decoratedItem.offsetPixel.x) > abandonOffset) ||
            (abs(decoratedItem.offsetPixel.y) > abandonOffset)) {
            endLongLongPress()
        }
        // Leak this action for ?
        return ActionHandler.ACT.IGNORED
    }

    override fun onActionSelect(pos: PointF): ActionHandler.ACT {
        progressItem.visible = true
        mPressSince = System.currentTimeMillis()
        mHandler.postDelayed(mRunnable, 100)

        return ActionHandler.ACT.HANDLED
    }
}

