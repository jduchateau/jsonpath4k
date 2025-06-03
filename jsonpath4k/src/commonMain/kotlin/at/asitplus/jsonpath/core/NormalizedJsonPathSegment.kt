package at.asitplus.jsonpath.core

import at.asitplus.jsonpath.generated.JsonPathLexer
import at.asitplus.jsonpath.generated.JsonPathParser
import kotlinx.serialization.Serializable
import org.antlr.v4.kotlinruntime.CharStreams
import org.antlr.v4.kotlinruntime.CommonTokenStream
import org.antlr.v4.kotlinruntime.ListTokenSource

/**
 * specification: https://datatracker.ietf.org/doc/rfc9535/
 * date: 2024-02
 * section: 2.7.  Normalized Paths
 */
@Serializable
sealed interface NormalizedJsonPathSegment {
    @Serializable
    class NameSegment(val memberName: String) : NormalizedJsonPathSegment {
        override fun toString(): String {
            return "[${Rfc9535Utils.escapeToSingleQuotedStringLiteral(memberName)}]"
        }

        /**
         * Throws an exception if using shorthand notation is not possible.
         */
        fun toShorthandNotation(): String {
            val tokens = JsonPathLexer(CharStreams.fromString(".$memberName")).allTokens

            val commonTokenStream = CommonTokenStream(ListTokenSource(tokens))
            val shorthandSegmentContext = JsonPathParser(commonTokenStream).shorthand_segment()

            shorthandSegmentContext.memberNameShorthand()?.let {
                if (it.MEMBER_NAME_SHORTHAND().text != memberName) {
                    null
                } else {
                    memberName
                }
            } ?: throw IllegalStateException(
                "Cannot represent member name ${
                    Rfc9535Utils.escapeToDoubleQuoted(memberName)
                } in shorthand notation."
            )

            return ".$memberName"
        }
    }

    @Serializable
    class IndexSegment(val index: UInt) : NormalizedJsonPathSegment {
        override fun toString(): String {
            return "[$index]"
        }
    }
}