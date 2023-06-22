package io.github.deficuet.alp

import io.github.deficuet.unitykt.UnityAssetManager
import io.github.deficuet.unitykt.data.AssetBundle
import io.github.deficuet.unitykt.data.GameObject
import io.github.deficuet.unitykt.firstOfOrNull
import io.github.deficuet.unitykt.getObj
import io.github.deficuet.unitykt.math.Vector2
import io.github.deficuet.unitykt.math.Vector4
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.math.round

internal class PreCheckStatus(
    private val manager: UnityAssetManager,
    private val bundle: AssetBundle,
    private val baseGameObject: GameObject,
    succeed: Boolean,
    message: String
): AnalyzeStatus(succeed, message) {
    operator fun component1() = manager
    operator fun component2() = bundle
    operator fun component3() = baseGameObject
}

internal fun Vector2.round() = Vector2(round(x), round(y))

internal inline fun <reified T> Any?.safeCast(): T? {
    return if (this is T) this else null
}

internal fun checkFile(filePath: Path): AnalyzeStatus {
    if (!Files.isRegularFile(filePath)) {
        return AnalyzeStatus(false, "该路径不是文件")
    }
    val manager = UnityAssetManager()
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
    val baseGameObject = bundle.mContainer[0].second.asset.getObj()
    if (baseGameObject == null || baseGameObject !is GameObject || baseGameObject.mTransform.isEmpty()) {
        return AnalyzeStatus(false)
    }
    return PreCheckStatus(manager, bundle, baseGameObject, true, "")
}

internal fun <T: TextureTransform> buildTransformTree(
    parent: ExtendedTransform,
    transformFactory: (ExtendedTransform) -> T?
): MutableList<T> {
    val result = mutableListOf<T>()
    val attach = transformFactory(parent)
    if (attach != null) {
        result.add(attach)
    }
    for (child in parent.tr.mChildren) {
        val rect = child.getObj()
        if (rect != null) {
            result.addAll(
                buildTransformTree(
                    ExtendedTransformChild(rect, parent),
                    transformFactory
                )
            )
        }
    }
    return result
}

/**
 * @return `Vector4(leftBoundary, bottomBoundary, width, height)`
 */
internal fun measureBoundary(transforms: List<TextureTransform>): Vector4 {
    val left = transforms.minOf { it.pastePoint.x }
    val bottom = transforms.minOf { it.pastePoint.y }
    val leftBottom = Vector2(left, bottom)
    val rightTopBoundaries = transforms.map { tr ->
        (tr.unscaledSize * tr.overallScale + tr.pastePoint).round()
    }
    val right = rightTopBoundaries.maxOf { it.x }
    val top = rightTopBoundaries.maxOf { it.y }
    return Vector4(leftBottom, right - left, top - bottom)
}
