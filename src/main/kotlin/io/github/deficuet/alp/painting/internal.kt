package io.github.deficuet.alp.painting

import io.github.deficuet.alp.ExtendedTransform
import io.github.deficuet.alp.buildTransformTree
import io.github.deficuet.alp.measureBoundary
import io.github.deficuet.unitykt.classes.GameObject

internal fun buildPaintingStack(root: GameObject): List<PaintingTransform> {
    val stack = mutableListOf<PaintingTransform>()
    buildTransformTree(ExtendedTransform(root.mTransform!!)) {
        PaintingTransform.createFrom(it)?.let { attach -> stack.add(attach) }
    }
    return stack
}

internal fun pasteCorrection(transforms: List<PaintingTransform>): PaintingAnalyzeResult {
    return with(measureBoundary(transforms)) {
        val c = vector2
        PaintingAnalyzeResult(
            z.toInt(), w.toInt(),
            transforms.onEach { it.pastePoint -= c }
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