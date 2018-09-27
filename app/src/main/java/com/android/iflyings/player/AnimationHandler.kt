package com.android.iflyings.player

import android.os.Handler
import android.os.Message

open class AnimationHandler(listener: OnAnimationListener) : Handler() {
    companion object {
        private const val MSG_ANIMATION = 1
    }

    private val mOnAnimationListener: OnAnimationListener = listener

    open fun scroll(frameCount: Int) {
        for (i in 0 until frameCount) {
            val message = obtainMessage(MSG_ANIMATION, i, frameCount)
            sendMessageDelayed(message, 16L * i)
        }
    }

    override fun handleMessage(message: Message) {
        if (MSG_ANIMATION == message.what) {
            when (message.arg1) {
                0 -> {
                    mOnAnimationListener.startAnimation()
                }
                message.arg2 - 1 -> {
                    mOnAnimationListener.endAnimation()
                }
                else -> {
                    mOnAnimationListener.updateAnimation( 1.0f * message.arg1 / message.arg2)
                }
            }
        }
    }

    interface OnAnimationListener {

        fun startAnimation()

        fun updateAnimation(ratio: Float)

        fun endAnimation()

    }
}