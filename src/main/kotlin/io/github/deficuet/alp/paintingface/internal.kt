package io.github.deficuet.alp.paintingface

import io.github.deficuet.alp.ExtendedTransform
import io.github.deficuet.alp.TextureTransform
import io.github.deficuet.alp.buildTransformTree
import io.github.deficuet.alp.painting.PaintingTemplateTransform
import io.github.deficuet.alp.painting.PaintingTransform
import io.github.deficuet.alp.round
import io.github.deficuet.unitykt.classes.GameObject
import io.github.deficuet.unitykt.math.Vector2
import io.github.deficuet.unitykt.pptr.getObj

internal fun buildPaintingfaceStack(root: GameObject): PaintingfaceStackInfo {
    val stack = mutableListOf<PaintingTemplateTransform>()
    var faceRect: PaintingfaceTransform? = null
    buildTransformTree(ExtendedTransform(root.mTransform!!)) {
        val gameObject = it.tr.mGameObject.getObj()
        if (gameObject.mName == "face") {
            faceRect = PaintingfaceTransform(
                it.unscaledSize,
                it.overallScale,
                it.origin.round()
            )
        } else {
            PaintingTransform.createTemplateFrom(it)?.let { attach -> stack.add(attach) }
        }
    }
    return PaintingfaceStackInfo(stack, faceRect)
}

internal class PaintingfaceTransform(
    unscaledSize: Vector2,
    overallScale: Vector2,
    pastePoint: Vector2
): TextureTransform(unscaledSize, overallScale, pastePoint)

internal class PaintingfaceStackInfo(
    val paintingStack: List<PaintingTemplateTransform>,
    val faceRect: PaintingfaceTransform?
)
