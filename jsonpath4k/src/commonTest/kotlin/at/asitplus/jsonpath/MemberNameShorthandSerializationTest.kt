package at.asitplus.jsonpath

import at.asitplus.jsonpath.core.NormalizedJsonPathSegment
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FreeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe

class MemberNameShorthandSerializationTest : FreeSpec({
    "should be serializable as member name shorthand" - {
        withData(
            "test",
            "test_123",
            "t1",
        ) {
            shouldNotThrowAny {
                NormalizedJsonPathSegment.NameSegment(it).toShorthandNotation()
            } shouldBe ".$it"
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
            shouldThrowAny {
                NormalizedJsonPathSegment.NameSegment(it).toShorthandNotation() shouldBe ".$it"
            }
        }
    }
})