package io.github.deficuet.alp

import io.github.deficuet.alp.painting.PaintingAnalyzeStatus
import io.github.deficuet.alp.painting.analyzePainting
import io.github.deficuet.alp.painting.decoratePainting
import io.github.deficuet.alp.painting.rebuildPainting
import io.github.deficuet.alp.paintingface.PaintingfaceAnalyzeStatus
import io.github.deficuet.alp.paintingface.analyzePaintingface
import io.github.deficuet.alp.paintingface.scalePaintingface
import io.github.deficuet.jimage.BufferedImage
import io.github.deficuet.jimage.flipY
import io.github.deficuet.jimage.paste
import io.github.deficuet.jimage.savePng
import io.github.deficuet.unitykt.allObjectsOf
import io.github.deficuet.unitykt.data.Texture2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.io.path.Path

fun main() {
    val root = "C:\\Users\\Defic\\self\\Programs\\ALAssetsDownloader\\AssetBundles\\painting"
    val faceRoot = "C:\\Users\\Defic\\self\\Programs\\ALAssetsDownloader\\AssetBundles\\paintingface"
    val dest = "C:\\Users\\Defic\\self\\GameAssets\\AzurLane\\ship\\painting\\Tem"
    val fileList = listOf<String>(
        "xiefeierde_4", "bisimaiz", "aijier_3"
    )
    for (fileName in fileList) {
        with(analyzePaintingface(Path("$root/$fileName")) as PaintingfaceAnalyzeStatus) {
            val painting = ImageIO.read(File("$dest/${fileName}.png")).flipY()
            val base = if (result.width > painting.width || result.height > painting.height) {
                println("\u001b[31m extended \u001b[0m")
                BufferedImage(result.width, result.height, BufferedImage.TYPE_4BYTE_ABGR).paste(
                    painting,
                    result.transforms[0].pastePoint.x.toInt(),
                    result.transforms[0].pastePoint.y.toInt()
                )
            } else painting
            manager.use {
                val faceContext = it.loadFile("$faceRoot/$fileName")
                val faceList = faceContext.objectList.filterIsInstance<Texture2D>()
                with(result.transforms[1]) {
                    base.paste(
                        scalePaintingface(faceList.random().image, this),
                        pastePoint.x.toInt(), pastePoint.y.toInt()
                    )
                }.flipY().savePng("$dest/${fileName}_exp.png")
            }
        }
    }
}

fun main1() {
    val root = "C:\\Users\\Defic\\self\\Programs\\ALAssetsDownloader\\AssetBundles\\painting"
    val dest = "C:\\Users\\Defic\\self\\GameAssets\\AzurLane\\ship\\painting\\Tem"
    val fileList = listOf<String>(
        "xiefeierde_4"
    )
    for (fileName in fileList) {
        with(analyzePainting(Path("$root/$fileName")) as PaintingAnalyzeStatus) {
            val ret = BufferedImage(result.width, result.height, BufferedImage.TYPE_4BYTE_ABGR) {
                manager.use { m ->
                    for (tr in result.transforms) {
                        drawImage(
                            decoratePainting(rebuildPainting(m, tr), tr),
                            tr.pastePoint.x.toInt(), tr.pastePoint.y.toInt(), null
                        )
                    }
                }
            }.flipY().savePng("$dest/${fileName}.png")
        }
    }
}