package io.github.deficuet.alp

import io.github.deficuet.unitykt.UnityAssetManager
import io.github.deficuet.unitykt.classes.AssetBundle
import io.github.deficuet.unitykt.classes.GameObject
import io.github.deficuet.unitykt.firstOfOrNull
import io.github.deficuet.unitykt.math.Vector2
import io.github.deficuet.unitykt.math.Vector4
import io.github.deficuet.unitykt.pptr.safeGetObj
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.math.round

internal class PreCheckStatus(
    private val bundle: AssetBundle,
    private val baseGameObject: GameObject,
    succeed: Boolean,
    message: String
): AnalyzeStatus(succeed, message) {
    operator fun component1() = bundle
    operator fun component2() = baseGameObject
}

internal fun Vector2.round() = Vector2(round(x), round(y))

internal fun checkFile(filePath: Path, manager: UnityAssetManager): AnalyzeStatus {
    if (!Files.isRegularFile(filePath)) {
        return AnalyzeStatus(false, "该路径不是文件")
    }
    val context = try {
        manager.loadFile(filePath.absolutePathString())
    } catch (e: Exception) {
        return AnalyzeStatus(false, "加载AssetBundle失败")
    }
    if (context.objectList.isEmpty()) {
        return AnalyzeStatus(false)
    }
    val bundle = context.objectList.firstOfOrNull<AssetBundle>()
        ?: return AnalyzeStatus(false)
    val baseGameObject = bundle.mContainer.values.first()[0].asset.safeGetObj()
    if (baseGameObject == null || baseGameObject !is GameObject || baseGameObject.mTransform == null) {
        return AnalyzeStatus(false)
    }
    return PreCheckStatus(bundle, baseGameObject, true, "")
}

internal fun buildTransformTree(
    parent: ExtendedTransform,
    transformFactory: (ExtendedTransform) -> Unit
) {
    transformFactory(parent)
    for (child in parent.tr.mChildren) {
        val rect = child.safeGetObj()
        if (rect != null) {
            buildTransformTree(
                ExtendedTransformChild(rect, parent),
                transformFactory
            )
        }
    }
}

/**
 * @return `Vector4(leftBoundary, bottomBoundary, width, height)`
 */
internal fun measureBoundary(transforms: List<TextureTransform>): Vector4 {
    val left = transforms.minOf { it.pastePoint.x }
    val bottom = transforms.minOf { it.pastePoint.y }
//    val leftBottom = Vector2(left, bottom)
    val rightTopBoundaries = transforms.map { tr ->
        (tr.unscaledSize * tr.overallScale + tr.pastePoint).round()
    }
    val right = rightTopBoundaries.maxOf { it.x }
    val top = rightTopBoundaries.maxOf { it.y }
    return Vector4(left, bottom, right - left, top - bottom)
}
