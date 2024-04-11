package io.github.deficuet.alp

import io.github.deficuet.unitykt.UnityAssetManager

open class AnalyzeStatus internal constructor(
    val succeed: Boolean,
    val message: String = "AssetBundle不可用"
)

open class DependencyMissing internal constructor(
    succeed: Boolean = false,
    message: String = "依赖项缺失",
    val dependencies: Map<String, Boolean>
): AnalyzeStatus(succeed, message)

class AnalyzeResult internal constructor(
    val width: Int,
    val height: Int,
    val isStacked: Boolean,
    val stack: List<TextureTransform>
)

class PaintingAnalyzeStatus internal constructor(
    dependencies: Map<String, Boolean>,
    val result: AnalyzeResult,
    val manager: UnityAssetManager
): DependencyMissing(true, "", dependencies)
