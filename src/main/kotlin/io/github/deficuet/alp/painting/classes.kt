package io.github.deficuet.alp.painting

import io.github.deficuet.alp.*
import io.github.deficuet.unitykt.UnityAssetManager
import io.github.deficuet.unitykt.classes.GameObject
import io.github.deficuet.unitykt.classes.Mesh
import io.github.deficuet.unitykt.classes.MonoBehaviour
import io.github.deficuet.unitykt.classes.Sprite
import io.github.deficuet.unitykt.math.Vector2
import io.github.deficuet.unitykt.pptr.firstOfOrNull
import io.github.deficuet.unitykt.pptr.getObj
import io.github.deficuet.unitykt.pptr.safeGetObj
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
    val transform: PaintingTransform,
    val manager: UnityAssetManager
): AnalyzeStatusDep(succeed, message, dependencies)

class PaintingAnalyzeStatus internal constructor(
    dependencies: Map<String, Boolean>,
    val result: AnalyzeResult<PaintingTransform>,
    val manager: UnityAssetManager
): AnalyzeStatusDep(true, "", dependencies)

class PaintingTransform private constructor(
    val fileName: String,
    val sprite: Sprite,
    val mesh: Mesh?,
    val rawPaintingSize: Vector2,
    unscaledSize: Vector2,
    overallScale: Vector2,
    pastePoint: Vector2
): TextureTransform(unscaledSize, overallScale, pastePoint) {
    internal companion object {
        internal fun getMonoBehaviour(gameObject: GameObject): JSONObject? {
            return gameObject.mComponents.firstOfOrNull<MonoBehaviour>()?.toTypeTreeJson()
        }

        internal fun createFrom(tr: ExtendedTransform): PaintingTransform? {
            val gameObject = tr.tr.mGameObject.getObj()
            if (!gameObject.mIsActive) return null
            return getMonoBehaviour(gameObject)?.let { mono ->
                if ("m_Sprite" in mono.keySet() && "mMesh" in mono.keySet()) {
                    with(mono.getJSONObject("m_Sprite")) {
                        tr.tr.createPPtr<Sprite>(
                            getInt("m_FileID"),
                            getLong("m_PathID")
                        ).safeGetObj()
                    }?.let { spriteObj ->
                        PaintingTransform(
                            spriteObj.mName, spriteObj,
                            with(mono.getJSONObject("mMesh")) {
                                tr.tr.createPPtr<Mesh>(
                                    getInt("m_FileID"),
                                    getLong("m_PathID")
                                ).safeGetObj()
                            },
                            mono.getJSONObject("mRawSpriteSize").let {
                                Vector2(it.getFloat("x"), it.getFloat("y"))
                            },
                            tr.unscaledSize, tr.overallScale, tr.origin.round()
                        )
                    }
                } else null
            }
        }
    }
}
