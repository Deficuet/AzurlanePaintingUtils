package io.github.deficuet.alp.painting

import io.github.deficuet.alp.AnalyzeResult
import io.github.deficuet.alp.ExtendedTransform
import io.github.deficuet.alp.buildTransformTree
import io.github.deficuet.alp.measureBoundary
import io.github.deficuet.unitykt.data.GameObject

internal fun buildPaintingStack(root: GameObject): List<PaintingTransform> {
    return buildTransformTree(
        ExtendedTransform(root.mTransform[0]),
        PaintingTransform.Companion::createFrom
    )
}

internal fun pasteCorrection(transforms: List<PaintingTransform>): AnalyzeResult<PaintingTransform> {
    return with(measureBoundary(transforms)) {
        AnalyzeResult(
            z.toInt(), w.toInt(),
            transforms.onEach { it.pastePoint -= vector2 }
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