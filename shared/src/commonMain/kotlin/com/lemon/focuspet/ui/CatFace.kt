package com.lemon.focuspet.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.lemon.focuspet.model.PetState

private val FaceColor = Color(0xFFFFF3E0)
private val EarInnerColor = Color(0xFFFCE4EC)
private val OutlineColor = Color(0xFF5D4037)
private val EyeWhite = Color.White
private val PupilColor = Color(0xFF3E2723)
private val NoseColor = Color(0xFFF06292)
private val BlushColor = Color(0xFFFFCDD2)

@Composable
fun CatFace(
    state: PetState,
    eyeDx: Float,
    eyeDy: Float,
    blink: Boolean,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f + size.width * 0.04f
        val s = size.width

        // ── Ears ──
        val earTipY = cy - s * 0.38f
        val earBaseY = cy - s * 0.22f
        val earTipOffX = s * 0.32f
        val earBaseInnerX = s * 0.16f
        val earBaseOuterX = s * 0.38f
        val earInset = s * 0.02f

        drawEar(cx - earTipOffX, earTipY, cx - earBaseInnerX, earBaseY, cx - earBaseOuterX, earBaseY)
        drawEar(cx - earTipOffX + earInset, earTipY + earInset, cx - earBaseInnerX + earInset, earBaseY - earInset, cx - earBaseOuterX + earInset, earBaseY - earInset, EarInnerColor)

        drawEar(cx + earTipOffX, earTipY, cx + earBaseOuterX, earBaseY, cx + earBaseInnerX, earBaseY)
        drawEar(cx + earTipOffX - earInset, earTipY + earInset, cx + earBaseOuterX - earInset, earBaseY - earInset, cx + earBaseInnerX - earInset, earBaseY - earInset, EarInnerColor)

        // ── Head ──
        val headR = s * 0.30f
        drawCircle(FaceColor, headR, Offset(cx, cy))
        drawCircle(OutlineColor, headR, Offset(cx, cy), style = Stroke(s * 0.015f))

        // ── Eyes ──
        val eyeY = cy - s * 0.03f
        val eyeSpacing = s * 0.11f
        val eyeW = s * 0.09f
        val eyeH = s * 0.11f
        val pupilR = s * 0.035f
        val pupilMaxDx = (eyeW / 2f - pupilR - s * 0.005f).coerceAtLeast(0f)
        val pupilMaxDy = (eyeH / 2f - pupilR - s * 0.005f).coerceAtLeast(0f)
        val pDx = eyeDx * pupilMaxDx
        val pDy = eyeDy * pupilMaxDy
        val strokeW = s * 0.01f

        fun drawEye(centerX: Float) {
            drawOval(EyeWhite, Offset(centerX - eyeW / 2f, eyeY - eyeH / 2f), Size(eyeW, eyeH))
            drawOval(OutlineColor, Offset(centerX - eyeW / 2f, eyeY - eyeH / 2f), Size(eyeW, eyeH), style = Stroke(strokeW))
            if (!blink) {
                drawCircle(PupilColor, pupilR, Offset(centerX + pDx, eyeY + pDy))
            } else {
                drawLine(OutlineColor, Offset(centerX - eyeW / 2f + strokeW, eyeY), Offset(centerX + eyeW / 2f - strokeW, eyeY), strokeW * 1.5f)
            }
        }

        drawEye(cx - eyeSpacing)
        drawEye(cx + eyeSpacing)

        // ── Eyebrows ──
        val browY = eyeY - s * 0.08f
        val browW = s * 0.07f
        val browSW = s * 0.018f

        when (state) {
            PetState.ANGRY -> {
                drawLine(OutlineColor, Offset(cx - eyeSpacing - browW, browY + s * 0.02f), Offset(cx - eyeSpacing + browW, browY - s * 0.02f), browSW)
                drawLine(OutlineColor, Offset(cx + eyeSpacing + browW, browY + s * 0.02f), Offset(cx + eyeSpacing - browW, browY - s * 0.02f), browSW)
            }
            PetState.PLEADING -> {
                drawLine(OutlineColor, Offset(cx - eyeSpacing - browW, browY - s * 0.02f), Offset(cx - eyeSpacing + browW, browY + s * 0.02f), browSW)
                drawLine(OutlineColor, Offset(cx + eyeSpacing + browW, browY - s * 0.02f), Offset(cx + eyeSpacing - browW, browY + s * 0.02f), browSW)
            }
            else -> {
                drawLine(OutlineColor, Offset(cx - eyeSpacing - browW, browY), Offset(cx - eyeSpacing + browW, browY), browSW)
                drawLine(OutlineColor, Offset(cx + eyeSpacing - browW, browY), Offset(cx + eyeSpacing + browW, browY), browSW)
            }
        }

        // ── Blush ──
        if (state == PetState.HAPPY || state == PetState.PLEADING) {
            val blushR = s * 0.04f
            val blushAlpha = 0.5f
            drawCircle(BlushColor.copy(alpha = blushAlpha), blushR, Offset(cx - eyeSpacing - s * 0.06f, eyeY + s * 0.06f))
            drawCircle(BlushColor.copy(alpha = blushAlpha), blushR, Offset(cx + eyeSpacing + s * 0.06f, eyeY + s * 0.06f))
        }

        // ── Nose ──
        val noseY = eyeY + s * 0.08f
        val noseH = s * 0.04f
        val noseW = s * 0.03f
        val nosePath = Path().apply {
            moveTo(cx - noseW, noseY + noseH)
            lineTo(cx, noseY)
            lineTo(cx + noseW, noseY + noseH)
            close()
        }
        drawPath(nosePath, NoseColor)
        drawPath(nosePath, OutlineColor, style = Stroke(strokeW))

        // ── Mouth ──
        val mouthY = noseY + s * 0.05f
        val mouthPath = Path()
        val ms = s * 0.05f
        val msw = s * 0.015f

        when (state) {
            PetState.HAPPY -> {
                mouthPath.moveTo(cx - ms, mouthY - s * 0.01f)
                mouthPath.quadraticTo(cx, mouthY + s * 0.06f, cx + ms, mouthY - s * 0.01f)
                drawPath(mouthPath, OutlineColor, style = Stroke(msw))
            }
            PetState.FOCUSING -> {
                mouthPath.moveTo(cx - ms * 0.6f, mouthY)
                mouthPath.quadraticTo(cx, mouthY + s * 0.03f, cx + ms * 0.6f, mouthY)
                drawPath(mouthPath, OutlineColor, style = Stroke(msw))
            }
            PetState.ANGRY -> {
                mouthPath.moveTo(cx - ms, mouthY + s * 0.01f)
                mouthPath.quadraticTo(cx, mouthY - s * 0.04f, cx + ms, mouthY + s * 0.01f)
                drawPath(mouthPath, OutlineColor, style = Stroke(msw))
            }
            PetState.PLEADING -> {
                drawCircle(OutlineColor, s * 0.015f, Offset(cx, mouthY))
            }
            PetState.RESTING -> {
                mouthPath.moveTo(cx - ms * 0.6f, mouthY)
                mouthPath.quadraticTo(cx - s * 0.01f, mouthY + s * 0.02f, cx, mouthY)
                mouthPath.moveTo(cx + s * 0.01f, mouthY)
                mouthPath.quadraticTo(cx + ms * 0.6f, mouthY + s * 0.02f, cx + ms * 0.6f, mouthY)
                drawPath(mouthPath, OutlineColor, style = Stroke(msw))
            }
        }

        // ── Whiskers ──
        val whiskerY = noseY + s * 0.02f
        val whiskerLen = s * 0.12f
        for (i in -1..1) {
            val wy = whiskerY + i * s * 0.03f
            val whiskerAlpha = 0.35f
            drawLine(OutlineColor.copy(alpha = whiskerAlpha), Offset(cx - headR + s * 0.02f, wy), Offset(cx - headR - whiskerLen, wy - i * s * 0.01f), strokeW * 0.8f)
            drawLine(OutlineColor.copy(alpha = whiskerAlpha), Offset(cx + headR - s * 0.02f, wy), Offset(cx + headR + whiskerLen, wy - i * s * 0.01f), strokeW * 0.8f)
        }
    }
}

private fun DrawScope.drawEar(
    tipX: Float, tipY: Float,
    baseLx: Float, baseLy: Float,
    baseRx: Float, baseRy: Float,
    fill: Color = FaceColor,
) {
    val path = Path().apply {
        moveTo(tipX, tipY)
        lineTo(baseLx, baseLy)
        lineTo(baseRx, baseRy)
        close()
    }
    drawPath(path, fill)
    drawPath(path, OutlineColor, style = Stroke(size.width * 0.015f))
}
