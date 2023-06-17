package io.github.deficuet.alp.painting

import io.github.deficuet.alp.*
import io.github.deficuet.unitykt.*
import io.github.deficuet.unitykt.data.MonoBehaviour
import io.github.deficuet.unitykt.math.Vector2
import org.json.JSONObject

open class AnalyzeStatusDep internal constructor(
    succeed: Boolean = false,
    message: String = "依赖项缺失",
    val dependencies: Map<String, Boolean>
): AnalyzeStatus(succeed, message)

class AnalyzeStatusStacks internal constructor(
    succeed: Boolean,
    message: String,
    dependencies: Map<String, Boolean>,
    val transforms: List<PaintingTransform>,
): AnalyzeStatusDep(succeed, message, dependencies)

class PaintingAnalyzeStatus internal constructor(
    dependencies: Map<String, Boolean>,
    val result: AnalyzeResult<PaintingTransform>,
    val manager: UnityAssetManager
): AnalyzeStatusDep(true, "", dependencies)

class PaintingTransform private constructor(
    val fileName: String,
    val sprite: Long,
    val mesh: Long,
    val rawPaintingSize: Vector2,
    unscaledSize: Vector2,
    overallScale: Vector2,
    pastePoint: Vector2
): TextureTransform(unscaledSize, overallScale, pastePoint) {
    internal companion object {
        internal fun getMonoBehaviour(tr: ExtendedTransform): JSONObject? {
            return tr.tr.mGameObject.getObj()!!.mComponents
                .mapNotNull { it.getObj() }.firstOfOrNull<MonoBehaviour>()
                ?.typeTreeJson
        }

        internal fun createFrom(rect: ExtendedTransform): PaintingTransform? {
            return getMonoBehaviour(rect)?.let { mono ->
                    if ("m_Sprite" in mono.keySet()) {
                        val spritePathID = mono.getJSONObject("m_Sprite").getLong("m_PathID")
                        rect.tr.assetFile.root.manager.objects.find {
                            it.mPathID == spritePathID
                        }?.let { spriteObj ->
                            PaintingTransform(
                                spriteObj.assetFile.root.name,
                                spritePathID,
                                mono.getJSONObject("mMesh").getLong("m_PathID"),
                                mono.getJSONObject("mRawSpriteSize").let {
                                    Vector2(it.getDouble("x"), it.getDouble("y"))
                                },
                                rect.unscaledSize,
                                rect.overallScale,
                                rect.origin.round() // + Vector2(1.0, 1.0)
                            )
                        }
                    } else null
                }
        }
    }
}
