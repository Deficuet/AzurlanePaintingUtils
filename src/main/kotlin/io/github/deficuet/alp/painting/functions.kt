package io.github.deficuet.alp.painting

import io.github.deficuet.alp.*
import io.github.deficuet.jimage.*
import io.github.deficuet.unitykt.*
import io.github.deficuet.unitykt.data.*
import java.awt.image.*
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.math.roundToInt

fun analyzePainting(filePath: Path): AnalyzeStatus {
    val checkResult = checkFile(filePath)
    if (checkResult !is PreCheckStatus) return checkResult
    val (manager, bundle, baseGameObject) = checkResult
    val dependencies = mutableMapOf<String, Boolean>()
    var checkPassed = true
    if (bundle.mDependencies.isNotEmpty()) {
        for (dependency in bundle.mDependencies) {
            val fileName = dependency.split("/").last()
            val exist = filePath.parent.resolve(fileName).exists()
            dependencies[fileName] = exist
            if (!exist) checkPassed = false
        }
    }
    if (!checkPassed) {
        return AnalyzeStatusDep(dependencies = dependencies)
    }
    try {
        for (dependencyName in dependencies.keys) {
            manager.loadFile(
                filePath.parent.resolve(dependencyName).absolutePathString()
            )
        }
    } catch (e: Exception) {
        return AnalyzeStatusDep(message = "加载依赖文件时出错", dependencies = dependencies)
    }
    val stack = buildPaintingStack(baseGameObject)
    if (stack.size == 1) {
        return AnalyzeStatusStacks(false, "无需合并", dependencies, stack)
    }
    return PaintingAnalyzeStatus(
        dependencies,
        pasteCorrection(stack),
        manager
    )
}

fun rebuildPainting(manager: UnityAssetManager, tr: PaintingTransform): BufferedImage {
    val rawPainting = manager.objects.objectFromPathID<Sprite>(tr.sprite).mRD.texture.getObj()!!
    return if (tr.mesh == 0L) {
        rawPainting.image
    } else {
        rawPainting.rebuildPainting(
            manager.objects.objectFromPathID(tr.mesh)
        )
    }
}

fun decoratePainting(image: BufferedImage, tr: PaintingTransform): BufferedImage {
    val w = tr.rawPaintingSize.x.toInt()
    val h = tr.rawPaintingSize.y.toInt()
    return if (w > image.width || h > image.height) {
        BufferedImage(
            maxOf(w, image.width),
            maxOf(h, image.height),
            image.type
        ).paste(image, 0, 0)
    } else {
        image
    }.fancyResize(
        (maxOf(tr.unscaledSize.x.roundToInt(), w) * tr.overallScale.x).roundToInt(),
        (maxOf(tr.unscaledSize.y.roundToInt(), h) * tr.overallScale.y).roundToInt()
    )
}

fun Texture2D.rebuildPainting(mesh: Mesh): BufferedImage {
    val width = mesh.exportVertices.maxOf { it.x }.toInt() + 1
    val height = mesh.exportVertices.maxOf { it.y }.toInt() + 1
    val imgData = decompressedImageData
    val outArray = ByteArray(height * width * 4)
    for (i in mesh.exportVertices.indices step 4) {
        val v1 = mesh.exportUV[i]; val v2 = mesh.exportUV[i + 2]
        val (px, py) = mesh.exportVertices[i]
        val fixY = if (py == 0.0) 1 else 0
        val v1x = (v1[0] * mWidth).roundToInt()
        val v1y = (v1[1] * mHeight).roundToInt() + fixY
        val v2x = (v2[0] * mWidth).roundToInt()
        val v2y = (v2[1] * mHeight).roundToInt()
        if (v2x - v1x != 0 && v2y - v1y != 0) {
            imgData.copyBGRABlockTo(
                outArray, py.toInt(), px.toInt(), width,
                v1y, v2y, v1x, v2x, mWidth
            )
        }
    }
    return BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR).apply {
        data = Raster.createRaster(
            ComponentSampleModel(
                DataBuffer.TYPE_BYTE, width, height, 4,
                width * 4, intArrayOf(2, 1, 0, 3)
            ),
            DataBufferByte(outArray, outArray.size), null
        )
    }
}
