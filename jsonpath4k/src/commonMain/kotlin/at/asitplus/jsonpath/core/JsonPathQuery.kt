package at.asitplus.jsonpath.core

import kotlinx.serialization.json.JsonElement

interface JsonPathQuery {
    val isSingularQuery: Boolean

    operator fun invoke(currentNode: JsonElement, rootNode: JsonElement = currentNode): NodeList
}