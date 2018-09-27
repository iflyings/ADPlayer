package com.android.iflyings.player.info

import com.android.iflyings.player.model.FrameModel

class FrameInfo {

    private var mTexWidth: Int = 0
    private var mTexHeight: Int = 0

    sealed class Effect {
        object Default : Effect()
        object HDR : Effect()
        object Gray : Effect()
        object Old : Effect()
        object Emboss : Effect()

        fun update(frameModel: FrameModel) {
            when (this) {
                Effect.Default -> {
                    frameModel.reset()
                }
                Effect.HDR -> {
                    frameModel.setHDR()
                }
                Effect.Gray -> {
                    frameModel.setGrayPhoto()
                }
                Effect.Old -> {
                    frameModel.setOldPhoto()
                }
                Effect.Emboss -> {
                    frameModel.setEmboss()
                }
            }
        }
    }
}