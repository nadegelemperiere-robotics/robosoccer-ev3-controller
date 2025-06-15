/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   Scroller smooth positioning functions
   ------------------------------------------------------- */
package org.mantabots.robosoccer.ui.settings


/* Android includes */
import android.content.Context

/* Androidx includes */
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import androidx.recyclerview.widget.LinearSmoothScroller


class Scroller<T : RecyclerView.ViewHolder>(
    view: RecyclerView,
    adapter: ListAdapter<String, T>,
    context: Context
) {

    private val mView = view
    private val mAdapter = adapter
    private val mContext = context
    private var mHelper: SnapHelper

    /* ----------------------- Constructors ------------------------ */
    init {
        mView.layoutManager = LinearLayoutManager(mContext)
        mView.adapter = mAdapter
        mHelper = LinearSnapHelper()
        mHelper.attachToRecyclerView(mView)
    }
    
    fun scroll(position: Int) {
        mHelper.attachToRecyclerView(null)

        /* 2️⃣  Jump so the target row view exists (no animation) */
        mView.scrollToPosition(position)

        /* 3️⃣  When the layout pass finishes, compute EXACT pixel delta the helper wants
               to centre THIS very row, and perform a tiny smoothScrollBy.               */
        mView.post {
            val view  = mView.layoutManager!!.findViewByPosition(position) ?: return@post
            val delta = mHelper.calculateDistanceToFinalSnap(mView.layoutManager!!, view) ?: return@post
            if (delta[0] != 0 || delta[1] != 0) {
                mView.smoothScrollBy(delta[0], delta[1])
            }

            /* 4️⃣  Re-attach helper only AFTER the micro-scroll request was queued.
                    At this point the row is mathematically centred, so helper keeps it. */
            mHelper.attachToRecyclerView(mView)
        }

        val scroller = CenterSmoothScroller(mContext).apply {
           targetPosition = position
        }
        mView.layoutManager!!.startSmoothScroll(scroller)// 3️⃣ wait until the scroll really ends, THEN re-attach the helper
        mView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    recyclerView.removeOnScrollListener(this)   // one-shot
                    mHelper.attachToRecyclerView(mView)     // re-attach
                }
            }
        })
    }

}


/** A SmoothScroller that ends with the target view centred vertically. */
class CenterSmoothScroller(ctx: Context) : LinearSmoothScroller(ctx) {

    override fun calculateDtToFit(
        viewStart: Int,
        viewEnd: Int,
        boxStart: Int,
        boxEnd: Int,
        snapPreference: Int
    ): Int {
        val viewCenter = (viewStart + viewEnd) / 2
        val boxCenter  = (boxStart + boxEnd) / 2
        return boxCenter - viewCenter          // move so centres coincide
    }

    /** We only care about vertical snapping here. */
    override fun getVerticalSnapPreference() = SNAP_TO_START
}