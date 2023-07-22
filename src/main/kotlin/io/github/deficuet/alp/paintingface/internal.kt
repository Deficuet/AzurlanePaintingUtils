package io.github.deficuet.alp.paintingface

import io.github.deficuet.alp.ExtendedTransform
import io.github.deficuet.alp.TextureTransform
import io.github.deficuet.alp.buildTransformTree
import io.github.deficuet.alp.painting.PaintingTransform
import io.github.deficuet.alp.round
import io.github.deficuet.unitykt.data.GameObject
import io.github.deficuet.unitykt.getObj
import io.github.deficuet.unitykt.math.Vector2

internal fun buildPaintingfaceStack(root: GameObject): MutableList<TextureTransform> {
    return buildTransformTree(ExtendedTransform(root.mTransform[0])) {
        val mono = PaintingTransform.getMonoBehaviour(it)
        if (
            it.tr.mGameObject.getObj().mName == "face"
        ) {
            PaintingfaceTransform(
                it.unscaledSize,
                it.overallScale,
                it.origin.round()
            )
        } else if (mono != null && "m_Sprite" in mono.keySet()) {
            TextureTransform(
                it.unscaledSize,
                it.overallScale,
                it.origin.round()
            )
        } else null
    }
}

internal class PaintingfaceTransform(
    unscaledSize: Vector2,
    overallScale: Vector2,
    pastePoint: Vector2
): TextureTransform(unscaledSize, overallScale, pastePoint)
