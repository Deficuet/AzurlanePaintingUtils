package io.github.deficuet.alp

import io.github.deficuet.unitykt.data.RectTransform
import io.github.deficuet.unitykt.data.Transform
import io.github.deficuet.unitykt.math.Vector2

open class ExtendedTransform internal constructor(
    val tr: Transform
) {
    open val localScale
        get() = Vector2(1.0, 1.0)

    open val cumulativeScale
        get() = Vector2(1.0, 1.0)

    val overallScale
        get() = localScale * cumulativeScale

    open val pivotPosition: Vector2
        get() = if (tr is RectTransform) size * tr.mPivot else Vector2(0.0, 0.0)

    open val origin
        get() = Vector2.Zero

    open val unscaledSize: Vector2
        get() = if (tr is RectTransform) tr.mSizeDelta else Vector2(0.0, 0.0)

    open val size
        get() = unscaledSize
}

internal class ExtendedTransformChild(
    tr: Transform,
    private val parent: ExtendedTransform
): ExtendedTransform(tr) {
    override val localScale: Vector2
        get() = tr.mLocalScale.vector2

    override val cumulativeScale: Vector2
        get() = if (parent.tr is RectTransform) {
            parent.cumulativeScale
        } else {
            parent.overallScale
        }

    override val pivotPosition: Vector2
        get() = if (tr is RectTransform) {
            if (parent.tr is RectTransform) {
                with(tr) {
                    (mAnchorMax - mAnchorMin) * parent.size * mPivot +
                            mAnchorMin * parent.size + parent.origin +
                            mAnchoredPosition * cumulativeScale
                }
            } else {
                parent.pivotPosition + tr.mAnchoredPosition * cumulativeScale
            }
        } else {
            parent.pivotPosition + tr.mLocalPosition.vector2 * cumulativeScale
        }

    override val origin: Vector2
        get() {
            check(tr is RectTransform) { "\"rect\" must be RectTransform" }
            return with(tr) {
                pivotPosition - mPivot * size
            }
        }

    override val unscaledSize: Vector2
        get() = if (tr is RectTransform) {
            with(tr) {
                (mAnchorMax - mAnchorMin) * parent.unscaledSize + mSizeDelta
            }
        } else {
            Vector2.Zero
        }

    override val size: Vector2
        get() = unscaledSize * overallScale
}
