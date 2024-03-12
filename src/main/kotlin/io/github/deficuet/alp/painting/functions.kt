package io.github.deficuet.alp.painting

import io.github.deficuet.alp.AnalyzeStatus
import io.github.deficuet.alp.PreCheckStatus
import io.github.deficuet.alp.checkFile
import io.github.deficuet.jimage.fancyResize
import io.github.deficuet.jimage.paste
import io.github.deficuet.unitykt.UnityAssetManager
import io.github.deficuet.unitykt.classes.Mesh
import io.github.deficuet.unitykt.classes.Texture2D
import java.awt.image.*
import java.nio.file.Path
import kotlin.collections.set
import kotlin.io.path.exists
import kotlin.math.roundToInt

fun analyzePainting(filePath: Path, assetSystemRoot: Path, debugOutput: (String) -> Unit = {  }): AnalyzeStatus {
    val manager = UnityAssetManager.new(assetSystemRoot, debugOutput = debugOutput)
    val checkResult = checkFile(filePath, manager)
    if (checkResult !is PreCheckStatus) {
        manager.close()
        return checkResult
    }
    val (bundle, baseGameObject) = checkResult
    val dependencies = mutableMapOf<String, Boolean>()
    var checkPassed = true
    if (bundle.mDependencies.isNotEmpty()) {
        for (dependency in bundle.mDependencies) {
            val fileName = dependency.split("/").last()
            val exist = assetSystemRoot.resolve(dependency).exists()
            dependencies[fileName] = exist
            if (!exist) checkPassed = false
        }
    }
    if (!checkPassed) {
        return AnalyzeStatusDep(dependencies = dependencies)
    }
    val stack = buildPaintingStack(baseGameObject)
    if (stack.size == 1) {
        return AnalyzeStatusStacks(
            false, "无需合并",
            dependencies, stack[0], manager
        )
    }
    return PaintingAnalyzeStatus(
        dependencies,
        pasteCorrection(stack),
        manager
    )
}

fun rebuildPaintingFrom(tr: PaintingTransform): BufferedImage {
    val rawPainting = tr.sprite.getTexture2D()!!
    return if (tr.mesh == null) {
        rawPainting.getImage()!!
    } else {
        rebuildPainting(rawPainting, tr.mesh)
    }
}

fun decoratePainting(image: BufferedImage, tr: PaintingTemplateTransform): BufferedImage {
    val w = maxOf(tr.rawPaintingSize.x.toInt(), image.width)
    val h = maxOf(tr.rawPaintingSize.y.toInt(), image.height)
    return if (w > image.width || h > image.height) {
        BufferedImage(w, h, image.type).paste(image, 0, 0)
    } else {
        image
    }.fancyResize(
        (maxOf(tr.unscaledSize.x.roundToInt(), w) * tr.overallScale.x).roundToInt(),
        (maxOf(tr.unscaledSize.y.roundToInt(), h) * tr.overallScale.y).roundToInt()
    )
}

fun rebuildPainting(tex: Texture2D, mesh: Mesh): BufferedImage {
    val v = mesh.exportVertices()
    val width = v.maxOf { it.x }.toInt() + 1
    val height = v.maxOf { it.y }.toInt() + 1
    val imgData = tex.getDecompressedData()!!
    val outArray = ByteArray(height * width * 4)
    for (i in v.indices step 4) {
        val vt = mesh.exportUV()
        val v1 = vt[i]; val v2 = vt[i + 2]
        val (px, py) = v[i]
        val fixY = if (py == 0f) 1 else 0
        val v1x = (v1[0] * tex.mWidth).roundToInt()
        val v1y = (v1[1] * tex.mHeight).roundToInt() + fixY
        val v2x = (v2[0] * tex.mWidth).roundToInt()
        val v2y = (v2[1] * tex.mHeight).roundToInt()
        if (v2x - v1x != 0 && v2y - v1y != 0) {
            imgData.copyBGRABlockTo(
                outArray, py.toInt(), px.toInt(), width,
                v1y, v2y, v1x, v2x, tex.mWidth
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

