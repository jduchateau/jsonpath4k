package at.asitplus.jsonpath

import at.asitplus.jsonpath.core.NormalizedJsonPath
import at.asitplus.jsonpath.core.NormalizedJsonPathSegment
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FreeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class NormalizedJsonPathSerializationTest : FreeSpec({
    "normalized json path string" - {
        withData(
            "test" to "$['test']",
            "test_123" to "$['test_123']",
            "t1" to "$['t1']",
        ) { (it, expected) ->
            shouldNotThrowAny {
                val path = NormalizedJsonPath() + NormalizedJsonPathSegment.NameSegment(it)
                path.toNormalizedJsonPathString() shouldBe expected
            }
        }
    }
    "shorthand serialization" - {
        "should be serializable as member name shorthand" - {
            withData(
                "test",
                "test_123",
                "t1",
            ) {
                shouldNotThrowAny {
                    val path = NormalizedJsonPath() + NormalizedJsonPathSegment.NameSegment(it)
                    path.toShorthandNameSegmentNotation() shouldBe "$.$it"
                }
            }
        }
        "should not be serializable as member name shorthand" - {
            withData(
                "1",
                "*",
                "'",
                "\"",
                "test-data",
            ) {
                val path = NormalizedJsonPath() + NormalizedJsonPathSegment.NameSegment(it)
                shouldThrowAny {
                    path.toShorthandNameSegmentNotation()
                }
            }
        }
    }
})