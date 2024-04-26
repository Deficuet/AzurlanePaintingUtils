package io.github.deficuet.alp

import io.github.deficuet.unitykt.classes.GameObject
import io.github.deficuet.unitykt.classes.Mesh
import io.github.deficuet.unitykt.classes.MonoBehaviour
import io.github.deficuet.unitykt.classes.Sprite
import io.github.deficuet.unitykt.math.Vector2
import io.github.deficuet.unitykt.pptr.firstOfOrNull
import io.github.deficuet.unitykt.pptr.getObj
import io.github.deficuet.unitykt.pptr.safeGetObj
import org.json.JSONObject

enum class TextureType {
    PAINTING, FACE
}

open class TextureTransform internal constructor(
    val type: TextureType,
    private val tr: ExtendedTransform
) {
    var pastePoint = tr.origin.round()
        internal set

    val unscaledSize get() = tr.unscaledSize
    val overallScale get() = tr.overallScale
    val size get() = tr.size
}

class PaintingTransform private constructor(
    val fileName: String,
    val sprite: Sprite,
    val mesh: Mesh?,
    val rawPaintingSize: Vector2,
    type: TextureType,
    tr: ExtendedTransform
): TextureTransform(type, tr) {
    internal companion object {
        private val NAME_BANNED = setOf("shop_hx", "shadow")
        private const val NAME_BUILTIN_UI_SPRITE = "UISprite"

        private fun getMonoBehaviour(gameObject: GameObject): JSONObject? {
            return gameObject.mComponents.firstOfOrNull<MonoBehaviour>()?.toTypeTreeJson()
        }

        private inline fun <T> create(
            tr: ExtendedTransform,
            factory: (ExtendedTransform, JSONObject, Sprite, Boolean) -> T
        ): T? {
            val gameObject = tr.tr.mGameObject.getObj()
            if (gameObject.mName in NAME_BANNED) return null
            return getMonoBehaviour(gameObject)?.let { mono ->
                if ("m_Sprite" in mono.keySet()) {
                    with(mono.getJSONObject("m_Sprite")) {
                        tr.tr.createPPtr<Sprite>(
                            getInt("m_FileID"),
                            getLong("m_PathID")
                        ).safeGetObj()
                    }?.let { spriteObj ->
                        if ("mMesh" in mono.keySet()) {
                            factory(tr, mono, spriteObj, false)
                        } else {
                            if (spriteObj.mName == NAME_BUILTIN_UI_SPRITE) {
                                null
                            } else {
                                factory(tr, mono, spriteObj, true)
                            }
                        }
                    }
                } else null
            }
        }

        internal fun createFrom(tr: ExtendedTransform): PaintingTransform? {
            return create(tr) { etr, mono, spriteObj, ignoreMesh ->
                val mesh = if (ignoreMesh) null else with(mono.getJSONObject("mMesh")) {
                    tr.tr.createPPtr<Mesh>(
                        getInt("m_FileID"),
                        getLong("m_PathID")
                    ).safeGetObj()
                }
                val rawSize = if (mono.has("mRawSpriteSize")) {
                    mono.getJSONObject("mRawSpriteSize").let {
                        Vector2(it.getFloat("x"), it.getFloat("y"))
                    }
                } else {
                    spriteObj.getTexture2D()!!.let {
                        Vector2(it.mWidth.toFloat(), it.mHeight.toFloat())
                    }
                }
                PaintingTransform(
                    spriteObj.mName, spriteObj, mesh,
                    rawSize, TextureType.PAINTING, etr
                )
            }
        }
    }
}
