package com.example.filemanager

import android.content.Context
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar.SnackbarLayout
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import com.github.clans.fab.FloatingActionMenu

/**
 * Created by huangyu on 2017/5/30.
 */
class FloatingActionMenuBehavior(context: Context?, attrs: AttributeSet?) : CoordinatorLayout.Behavior<FloatingActionMenu>() {
    private var mTranslationY = 0f
    override fun layoutDependsOn(parent: CoordinatorLayout, child: FloatingActionMenu, dependency: View): Boolean {
        return dependency is SnackbarLayout
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: FloatingActionMenu, dependency: View): Boolean {
        updateTranslation(parent, child, dependency)
        return false
    }

    override fun onDependentViewRemoved(parent: CoordinatorLayout, child: FloatingActionMenu, dependency: View) {
        ViewCompat.animate(child).cancel()
        ViewCompat.animate(child).translationY(0f)
        ViewCompat.setTranslationY(child, 0f)
        mTranslationY = 0f
    }

    private fun updateTranslation(parent: CoordinatorLayout, child: FloatingActionMenu, dependency: View) {
        val translationY = getTranslationY(parent, child)
        if (translationY != mTranslationY) {
            ViewCompat.animate(child).cancel()
            if (Math.abs(translationY - mTranslationY) == dependency.height.toFloat()) {
                ViewCompat.animate(child).translationY(translationY)
            } else {
                ViewCompat.setTranslationY(child, translationY)
            }
            mTranslationY = translationY
        }
    }

    private fun getTranslationY(parent: CoordinatorLayout, child: FloatingActionMenu): Float {
        var minOffset = 0.0f
        val dependencies: List<*> = parent.getDependencies(child)
        var i = 0
        val z = dependencies.size
        while (i < z) {
            val view = dependencies[i] as View
            if (view is SnackbarLayout && parent.doViewsOverlap(child, view)) {
                minOffset = Math.min(minOffset, ViewCompat.getTranslationY(view) - view.getHeight().toFloat())
            }
            ++i
        }
        return minOffset
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionMenu,
                                     directTargetChild: View, target: View, nestedScrollAxes: Int): Boolean {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes)
    }

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionMenu, target: View,
                                dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed,
                dyUnconsumed)
        if (dyConsumed > 0 && !child.isMenuButtonHidden) {
            child.hideMenuButton(true)
        } else if (dyConsumed < 0 && child.isMenuButtonHidden) {
            child.showMenuButton(true)
        }
    }
}