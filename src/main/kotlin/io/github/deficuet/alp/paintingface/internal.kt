package io.github.deficuet.alp.paintingface

import io.github.deficuet.alp.ExtendedTransform
import io.github.deficuet.alp.TextureTransform
import io.github.deficuet.alp.buildTransformTree
import io.github.deficuet.alp.painting.PaintingTransform
import io.github.deficuet.alp.round
import io.github.deficuet.unitykt.classes.GameObject
import io.github.deficuet.unitykt.math.Vector2
import io.github.deficuet.unitykt.pptr.getObj

internal fun buildPaintingfaceStack(root: GameObject): MutableList<TextureTransform> {
    return buildTransformTree(ExtendedTransform(root.mTransform!!)) {
        val gameObjec = it.tr.mGameObject.getObj()
        val mono = PaintingTransform.getMonoBehaviour(gameObjec)
        if (gameObjec.mName == "face") {
            PaintingfaceTransform(
                it.unscaledSize,
                it.overallScale,
                it.origin.round()
            )
        } else if (
            gameObjec.mIsActive && mono != null &&
            "m_Sprite" in mono.keySet() && "mMesh" in mono.keySet()
        ) {
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
