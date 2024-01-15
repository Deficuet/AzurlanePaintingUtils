package io.github.deficuet.alp.paintingface

import io.github.deficuet.alp.*
import io.github.deficuet.jimage.fancyResize
import io.github.deficuet.unitykt.UnityAssetManager
import io.github.deficuet.unitykt.math.Vector2
import java.awt.image.BufferedImage
import java.nio.file.Path

fun analyzePaintingface(filePath: Path, assetSystemRoot: Path): AnalyzeStatus {
    val manager = UnityAssetManager.new(assetSystemRoot)
    val checkResult = checkFile(filePath, manager)
    if (checkResult !is PreCheckStatus) {
        manager.close()
        return checkResult
    }
    val (_, baseGameObject) = checkResult
    val stack = buildPaintingfaceStack(baseGameObject)
    val faceRect = stack.find { it is PaintingfaceTransform }
        ?: return AnalyzeStatus(false, "没有可用的数据")
    stack.remove(faceRect)
    val paintingBox = measureBoundary(stack)
    faceRect.pastePoint -= paintingBox.vector2
    val left = minOf(0f, faceRect.pastePoint.x)
    val bottom = minOf(0f, faceRect.pastePoint.y)
    val (faceRight, faceTop) = with(faceRect) {
        pastePoint + (unscaledSize * overallScale)
    }
    return PaintingfaceAnalyzeStatus(
        AnalyzeResult(
            (maxOf(paintingBox.z, faceRight) - left).toInt(),
            (maxOf(paintingBox.w, faceTop) - bottom).toInt(),
            listOf(
                TextureTransform(
                    Vector2(paintingBox.z, paintingBox.w),
                    Vector2(1f, 1f),
                    Vector2(-left, -bottom)
                ),
                faceRect.apply { pastePoint -= Vector2(left, bottom) }
            )
        ), manager, stack.size > 1
    )
}

class PaintingfaceAnalyzeStatus internal constructor(
    val result: AnalyzeResult<TextureTransform>,
    val manager: UnityAssetManager,
    val requiresMerge: Boolean
): AnalyzeStatus(true, "")

fun scalePaintingface(image: BufferedImage, tr: TextureTransform): BufferedImage {
    val size = (tr.unscaledSize * tr.overallScale).round()
    return image.fancyResize(size.x.toInt(), size.y.toInt())
}
