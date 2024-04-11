package io.github.deficuet.alp

import io.github.deficuet.unitykt.UnityAssetManager
import io.github.deficuet.unitykt.classes.AssetBundle
import io.github.deficuet.unitykt.classes.GameObject
import io.github.deficuet.unitykt.firstOfOrNull
import io.github.deficuet.unitykt.math.Vector2
import io.github.deficuet.unitykt.math.Vector4
import io.github.deficuet.unitykt.pptr.getObj
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

internal fun Vector2.round() = Vector2(round(x), round(y))

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

internal fun buildPaintingStack(root: GameObject, includeFace: Boolean): StackGroup {
    val stack = mutableListOf<TextureTransform>()
    var face: TextureTransform? = null
    var foundFace = false
    buildTransformTree(ExtendedTransform(root.mTransform!!)) {
        if (includeFace && !foundFace) {
            val gameObject = it.tr.mGameObject.getObj()
            if (gameObject.mName == "face") {
                foundFace = true
                face = TextureTransform(TextureType.FACE, it)
                stack.add(face!!)
                return@buildTransformTree
            }
        }
        PaintingTransform.createFrom(it)?.let { painting -> stack.add(painting) }
    }
    return StackGroup(stack, face)
}

/**
 * @return `Vector4(leftBoundary, bottomBoundary, width, height)`
 */
internal fun measureBoundary(transforms: List<TextureTransform>): Vector4 {
    val left = transforms.minOf { it.pastePoint.x }
    val bottom = transforms.minOf { it.pastePoint.y }
//    val leftBottom = Vector2(left, bottom)
    val rightTopBoundaries = transforms.map { tr ->
        (tr.size + tr.pastePoint).round()
    }
    val right = rightTopBoundaries.maxOf { it.x }
    val top = rightTopBoundaries.maxOf { it.y }
    return Vector4(left, bottom, right - left, top - bottom)
}

internal fun pasteCorrection(group: StackGroup): AnalyzeResult {
    return with(measureBoundary(group.stack)) {
        val c = vector2
        AnalyzeResult(
            z.toInt(), w.toInt(), group.stack.count { it.type == TextureType.PAINTING } > 1,
            group.apply { stack.onEach { it.pastePoint -= c } }
        )
    }
}

internal fun ByteArray.copyBGRABlockTo(
    dst: ByteArray, dstRow: Int, dstColumn: Int, dstWidth: Int,
    rowStart: Int, rowEnd: Int, columnStart: Int, columnEnd: Int, srcWidth: Int
) {
    val srcRowPixelCount = srcWidth * 4
    val dstRowPixelCount = dstWidth * 4
    val blockRowPixelCount = (columnEnd - columnStart) * 4
    val srcRowOffset = columnStart * 4
    val dstRowOffset = dstColumn * 4
    for ((i, row) in (rowStart until rowEnd).withIndex()) {
        System.arraycopy(
            this, row * srcRowPixelCount + srcRowOffset,
            dst, (dstRow + i) * dstRowPixelCount + dstRowOffset,
            blockRowPixelCount
        )
    }
}
