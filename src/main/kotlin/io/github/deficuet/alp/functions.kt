package io.github.deficuet.alp

import io.github.deficuet.jimage.fancyResize
import io.github.deficuet.jimage.paste
import io.github.deficuet.unitykt.UnityAssetManager
import io.github.deficuet.unitykt.classes.Mesh
import io.github.deficuet.unitykt.classes.Sprite
import io.github.deficuet.unitykt.classes.Texture2D
import io.github.deficuet.unitykt.math.Vector2
import java.awt.image.*
import java.nio.file.Path
import kotlin.collections.set
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.math.roundToInt

fun analyzePainting(
    filePath: Path,
    assetSystemRoot: Path,
    debugOutput: (String) -> Unit = {  }
): AnalyzeStatus {
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
            val dependencyPath = assetSystemRoot.resolve(dependency)
            val exist = dependencyPath.exists()
            dependencies[dependencyPath.name] = exist
            if (!exist) checkPassed = false
        }
    }
    if (!checkPassed) {
        return DependencyMissing(dependencies = dependencies)
    }
    val group = buildPaintingStack(baseGameObject)
    val result = pasteCorrection(group.stack)
    if (!result.isStacked) {
        //for guanghui only
        val face = group.faceRect!!
        face.pastePoint = Vector2(
            face.pastePoint.x / face.overallScale.x.coerceAtMost(1f),
            face.pastePoint.y / face.overallScale.y.coerceAtMost(1f)
        )
    }

    return PaintingAnalyzeStatus(dependencies, result, manager)
}

fun rebuildPaintingFrom(tr: PaintingTransform): BufferedImage {
    val rawPainting = tr.sprite.getTexture2D()!!
    return if (tr.mesh == null) {
        rawPainting.getImage()!!
    } else {
        rebuildPainting(rawPainting, tr.mesh)
    }
}

fun rebuildPainting(tex: Texture2D, mesh: Mesh): BufferedImage {
    val v = mesh.exportVertices()
    val vt = mesh.exportUV()
    val width = v.maxOf { it.x }.toInt() + 1
    val height = v.maxOf { it.y }.toInt() + 1
    val imgData = tex.getDecompressedData()!!
    val outArray = ByteArray(height * width * 4)
    for (i in v.indices step 4) {
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

fun decoratePainting(image: BufferedImage, tr: PaintingTransform): BufferedImage {
    val rawW = maxOf(tr.rawPaintingSize.x.toInt(), image.width)
    val rawH = maxOf(tr.rawPaintingSize.y.toInt(), image.height)
    return if (tr.mesh != null && (rawW > image.width || rawH > image.height)) {
        BufferedImage(rawW, rawH, image.type).paste(image, 0, 0)
    } else {
        image
    }.fancyResize(
        (maxOf(tr.unscaledSize.x.roundToInt(), rawW) * tr.overallScale.x).roundToInt(),
        (maxOf(tr.unscaledSize.y.roundToInt(), rawH) * tr.overallScale.y).roundToInt()
    )
}

fun getPaintingfaceImage(sprite: Sprite): BufferedImage {
    return with(sprite.mRD.atlasRectOffset) {
        if (x == -1f && y == -1f) {
            sprite.getTexture2D()!!.getImage()!!
        } else {
            sprite.getImage()!!
        }
    }
}

fun decoratePaintingface(image: BufferedImage, tr: TextureTransform, isStacked: Boolean): BufferedImage {
    val scale = if (isStacked) {
        tr.overallScale
    } else {
        with(tr.overallScale) {
            Vector2(
                x.coerceAtLeast(1f),
                y.coerceAtLeast(1f)
            )
        }
    }
    return with(tr.unscaledSize * scale) {
        image.fancyResize(x.roundToInt(), y.roundToInt())
    }
}


