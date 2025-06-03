package at.asitplus.jsonpath.core

import kotlinx.serialization.Serializable

/**
 * specification: https://datatracker.ietf.org/doc/rfc9535/
 * date: 2024-02
 * section: 2.7.  Normalized Paths
 */
@Serializable
class NormalizedJsonPath(
    val segments: List<NormalizedJsonPathSegment> = listOf(),
) {
    constructor(vararg segments: NormalizedJsonPathSegment) : this(segments = segments.asList())

    operator fun plus(other: NormalizedJsonPath): NormalizedJsonPath {
        return NormalizedJsonPath(this.segments + other.segments)
    }

    operator fun plus(segment: NormalizedJsonPathSegment) = this + NormalizedJsonPath(segment)

    operator fun plus(memberName: String) = this + NormalizedJsonPathSegment.NameSegment(memberName)

    operator fun plus(index: UInt) = this + NormalizedJsonPathSegment.IndexSegment(index)

    override fun toString(): String {
        return "$${segments.joinToString("")}"
    }

    /**
     * Throws an exception if using shorthand notation is not possible.
     */
    fun toShorthandNameSegmentNotation(): String {
        return "$${segments.joinToString("") {
            when(it) {
                is NormalizedJsonPathSegment.IndexSegment -> it.toString()
                is NormalizedJsonPathSegment.NameSegment -> it.toShorthandNotation()
            }
        }}"
    }
}