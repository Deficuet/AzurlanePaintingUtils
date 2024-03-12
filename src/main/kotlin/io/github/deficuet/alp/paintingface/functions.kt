package io.github.deficuet.alp.paintingface

import io.github.deficuet.alp.*
import io.github.deficuet.alp.painting.PaintingTemplateTransform
import io.github.deficuet.jimage.fancyResize
import io.github.deficuet.unitykt.UnityAssetManager
import io.github.deficuet.unitykt.math.Vector2
import java.awt.image.BufferedImage
import java.nio.file.Path

fun analyzePaintingface(filePath: Path, assetSystemRoot: Path, debugOutput: (String) -> Unit = {  }): AnalyzeStatus {
    val manager = UnityAssetManager.new(assetSystemRoot, debugOutput = debugOutput)
    val checkResult = checkFile(filePath, manager)
    if (checkResult !is PreCheckStatus) {
        manager.close()
        return checkResult
    }
    val (_, baseGameObject) = checkResult
    val info = buildPaintingfaceStack(baseGameObject)
    if (info.faceRect == null) return AnalyzeStatus(false, "没有可用的数据")
    val paintingBox = measureBoundary(info.paintingStack)
    info.faceRect.pastePoint -= paintingBox.vector2
    val left = minOf(0f, info.faceRect.pastePoint.x)
    val bottom = minOf(0f, info.faceRect.pastePoint.y)
    val (faceRight, faceTop) = with(info.faceRect) {
        pastePoint + (unscaledSize * overallScale)
    }
    return PaintingfaceAnalyzeStatus(
        PaintingfaceAnalysisResult(
            (maxOf(paintingBox.z, faceRight) - left).toInt(),
            (maxOf(paintingBox.w, faceTop) - bottom).toInt(),
            info.paintingStack,
            TextureTransform(
                Vector2(paintingBox.z, paintingBox.w),
                Vector2(1f, 1f),
                Vector2(-left, -bottom)
            ),
            info.faceRect.apply { pastePoint -= Vector2(left, bottom) }
        ), manager, info.paintingStack.size > 1
    )
}

class PaintingfaceAnalysisResult internal constructor(
    width: Int, height: Int, paintingStack: List<PaintingTemplateTransform>,
    val mergedRect: TextureTransform, val faceRect: TextureTransform
): AnalyzeResult<PaintingTemplateTransform>(width, height, paintingStack)

class PaintingfaceAnalyzeStatus internal constructor(
    val result: PaintingfaceAnalysisResult,
    val manager: UnityAssetManager,
    val requiresMerge: Boolean
): AnalyzeStatus(true, "")

fun scalePaintingface(image: BufferedImage, tr: TextureTransform): BufferedImage {
    val size = (tr.unscaledSize * tr.overallScale).round()
    return image.fancyResize(size.x.toInt(), size.y.toInt())
}
