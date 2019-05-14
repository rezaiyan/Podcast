package com.hezaro.wall.sdk.platform.player

import android.util.Pair
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer.DecoderInitializationException
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil.DecoderQueryException
import com.google.android.exoplayer2.util.ErrorMessageProvider

class PlayerErrorMessageProvider : ErrorMessageProvider<ExoPlaybackException> {

    val error_generic = "error_generic"
    val error_querying_decoders = "error_querying_decoders"
    val error_no_secure_decoder = "error_no_secure_decoder"
    val error_no_decoder = "error_no_decoder"
    val error_instantiating_decoder = "error_instantiating_decoder"
    override fun getErrorMessage(e: ExoPlaybackException): Pair<Int, String> {
        var errorString = error_generic
        if (e.type === ExoPlaybackException.TYPE_RENDERER) {
            val cause = e.rendererException
            if (cause is DecoderInitializationException) {
                // Special case for decoder initialization failures.
                errorString = if (cause.decoderName == null) {
                    when {
                        cause.cause is DecoderQueryException -> error_querying_decoders
                        cause.secureDecoderRequired -> error_no_secure_decoder
                        else -> error_no_decoder
                    }
                } else {
                    error_instantiating_decoder
                }
            }
        }
        return Pair.create(0, errorString)
    }
}