package io.github.deficuet.alp

//import io.github.deficuet.alp.painting.*
//import io.github.deficuet.alp.paintingface.PaintingfaceAnalyzeStatus
//import io.github.deficuet.alp.paintingface.analyzePaintingface
//import io.github.deficuet.alp.paintingface.scalePaintingface
//import io.github.deficuet.jimage.BufferedImage
//import io.github.deficuet.jimage.flipY
//import io.github.deficuet.jimage.paste
//import io.github.deficuet.jimage.save
//import io.github.deficuet.jimageio.savePng
//import io.github.deficuet.unitykt.cast
//import io.github.deficuet.unitykt.classes.Sprite
//import io.github.deficuet.unitykt.classes.Texture2D
//import io.github.deficuet.unitykt.pptr.getObj
//import java.awt.image.BufferedImage
//import java.io.File
//import java.util.UUID
//import javax.imageio.ImageIO
//import kotlin.io.path.Path
//
//fun maina() {
//    val systemRoot = Path("C:\\Users\\Defic\\self\\Programs\\ALAssetsDownloader\\AssetBundles")
//    val paintingRoot = systemRoot.resolve("painting")
//    val faceRoot = systemRoot.resolve("paintingface")
//    val dest = "C:\\Users\\Defic\\self\\GameAssets\\AzurLane\\ship\\painting\\Tem"
//    val fileList = listOf<String>(
////        "xiefeierde_4", "bisimaiz", "aijier_3", "nengdai_4", "ajiakesi_2",
//        "dunkeerke_3"//, "haiwangxing_2"
//    )
//    for (fileName in fileList) {
//        with(analyzePaintingface(paintingRoot.resolve(fileName), systemRoot) as PaintingfaceAnalyzeStatus) {
//            val painting = ImageIO.read(File("$dest/${fileName}.png")).flipY().apply(true)
//            val base = if (result.width > painting.width || result.height > painting.height) {
//                println("\u001b[31m extended \u001b[0m")
//                BufferedImage(result.width, result.height, BufferedImage.TYPE_4BYTE_ABGR).paste(
//                    painting,
//                    result.transforms[0].pastePoint.x.toInt(),
//                    result.transforms[0].pastePoint.y.toInt()
//                )
//            } else painting
//            manager.use {
//                val faceContext = it.loadFile(faceRoot.resolve(fileName))
//                val faceList = faceContext.objectList.filterIsInstance<Sprite>()
//                with(result.transforms[1]) {
//                    base.paste(
//                        scalePaintingface(faceList.random().getImage()!!, this),
//                        pastePoint.x.toInt(), pastePoint.y.toInt()
//                    )
//                }.flipY().apply(true).savePng(File("$dest/${fileName}_exp.png"), 9)
//            }
//        }
//    }
//}
//
//fun main5() {
//    val p = "feilikesishuerci_tex"
//    for (c in with(Charsets){ arrayOf(US_ASCII, UTF_8, UTF_16, UTF_16BE, UTF_16LE, UTF_32, UTF_32BE, UTF_32LE) }) {
//        println(UUID.nameUUIDFromBytes(p.toByteArray(c)))
//    }
//}

//fun main() {
//    val systemRoot = Path("C:\\Users\\Defic\\self\\Programs\\ALAssetsDownloader\\AssetBundles")
//    val root = systemRoot.resolve("painting")
//    val dest = "C:\\Users\\Defic\\self\\GameAssets\\AzurLane\\ship\\painting\\Tem"
//    val fileList = listOf<String>(
//        "feilikesishuerci",
//        "aijier_3",
//        "bisimaiz",
////        "lituoliao_3",
////        "nengdai_4",
////        "qiabayefu_2",
////        "quejie_4",
////        "xiefeierde_4",
////        "mayebuleize",
////        "yan_2",
//    )
//    for (fileName in fileList) {
//        val a = analyzePainting(root.resolve(fileName), systemRoot)
//        if (a !is PaintingAnalyzeStatus) {
//            println(a::class.java.name)
//            println(a.message)
//            continue
//        }
//        with(a) {
//            BufferedImage(result.width, result.height, BufferedImage.TYPE_4BYTE_ABGR) {
//                manager.use {
//                    for (tr in result.transforms) {
//                        drawImage(
//                            decoratePainting(rebuildPaintingFrom(tr), tr),
//                            tr.pastePoint.x.toInt(), tr.pastePoint.y.toInt(), null
//                        )
//                    }
//                }
//            }.flipY().apply(true).save(File("$dest/${fileName}.png"))
//        }
//    }
//}
//
//fun main() {
//    val root = "C:\\Users\\Defic\\self\\Programs\\ALAssetsDownloader\\AssetBundles\\painting"
//    val fileList = listOf<String>(
//        "aijiangcv"
//    )
//    val a = analyzePainting(Path("$root/${fileList[0]}"))
//    println(a)
//}