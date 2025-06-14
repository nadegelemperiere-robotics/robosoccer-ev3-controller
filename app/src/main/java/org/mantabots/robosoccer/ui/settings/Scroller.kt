package org.mantabots.robosoccer.ui.settings

import android.content.Context
import androidx.recyclerview.widget.LinearSmoothScroller


/** A SmoothScroller that ends with the target view centred vertically. */
private class CenterSmoothScroller(ctx: Context) : LinearSmoothScroller(ctx) {

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