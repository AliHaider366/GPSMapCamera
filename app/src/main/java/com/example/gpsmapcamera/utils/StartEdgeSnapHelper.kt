package com.example.gpsmapcamera.utils

import android.view.View
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView

class StartEdgeSnapHelper(private val offset: Int) : LinearSnapHelper() {
    override fun calculateDistanceToFinalSnap(
        layoutManager: RecyclerView.LayoutManager,
        targetView: View
    ): IntArray? {
        val out = IntArray(2)
        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToStart(targetView, layoutManager, offset)
        }
        if (layoutManager.canScrollVertically()) {
            out[1] = distanceToStart(targetView, layoutManager, offset)
        }
        return out
    }

    private fun distanceToStart(targetView: View, layoutManager: RecyclerView.LayoutManager, offset: Int): Int {
        return layoutManager.getDecoratedLeft(targetView) - offset
    }
}
