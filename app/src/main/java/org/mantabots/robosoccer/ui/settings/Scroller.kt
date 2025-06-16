/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   Scroller smooth positioning functions
   ------------------------------------------------------- */
package org.mantabots.robosoccer.ui.settings

/* Android includes */
import android.content.Context
import androidx.core.view.doOnLayout

/* Androidx includes */
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper


class Scroller<T : RecyclerView.ViewHolder>(
    view: RecyclerView,
    adapter: ListAdapter<String, T>,
    private val select: (String) -> Unit,
    context: Context
) {

    private val mView = view
    private val mAdapter = adapter
    private val mContext = context
    private var mHelper: SnapHelper
    private var mSelected = ""

    /* ----------------------- Constructors ------------------------ */
    init {
        mView.layoutManager = LinearLayoutManager(mContext)
        mView.adapter = mAdapter
        mHelper = LinearSnapHelper()
        mHelper.attachToRecyclerView(mView)

        mView.addOnScrollListener(
            SnapPositionListener(mHelper) { pos ->
                val item = mAdapter.currentList[pos]
                mSelected = item
                select(item)
            }
        )

        /* optional: trigger once on first layout so initial value is reported */
        mView.doOnLayout {
            val lm   = mView.layoutManager!!
            val view = mHelper.findSnapView(lm) ?: return@doOnLayout
            val pos  = lm.getPosition(view)
            mAdapter.currentList.getOrNull(pos)?.let { first ->
                mSelected = first
                select(first)
            }
        }
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

    }

    fun selected() : String {
        return mSelected
    }

}

/** Fires `onSnap` exactly once every time the snapped position changes. */
class SnapPositionListener(
    private val snapHelper: SnapHelper,
    private val onSnap: (position: Int) -> Unit
) : RecyclerView.OnScrollListener() {

    private var lastPos = RecyclerView.NO_POSITION

    override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            val lm   = rv.layoutManager ?: return
            val view = snapHelper.findSnapView(lm) ?: return
            val pos  = lm.getPosition(view)
            if (pos != lastPos) {
                lastPos = pos
                onSnap(pos)                 // 👉 notify caller
            }
        }
    }
}