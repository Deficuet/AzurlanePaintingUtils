package io.github.deficuet.alp

import io.github.deficuet.unitykt.math.Vector2

open class AnalyzeStatus internal constructor(
    val succeed: Boolean,
    val message: String = "AssetBundle不可用"
)

open class TextureTransform internal constructor(
    val unscaledSize: Vector2,
    val overallScale: Vector2,
    pastePoint: Vector2
) {
    var pastePoint = pastePoint
        internal set
}

class AnalyzeResult<T: TextureTransform> internal constructor(
    val width: Int,
    val height: Int,
    val transforms: List<T>
)
