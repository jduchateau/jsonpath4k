package at.asitplus.jsonpath

import at.asitplus.jsonpath.core.JsonPathCompiler

@Deprecated("Dependencies have been moved to JsonPath.Companion")
object JsonPathDependencyManager {
    @Deprecated(
        "To be removed in version 4. Dependencies have been moved to JsonPath.Companion",
        ReplaceWith("JsonPath.defaultFunctionExtensionRepository"),
    )
    var functionExtensionRepository: JsonPathFunctionExtensionRepository
        get() = JsonPath.defaultFunctionExtensionRepository
        set(value) {
            JsonPath.defaultFunctionExtensionRepository = value
        }

    @Deprecated(
        "To be removed in version 4. Dependencies have been moved to JsonPath.Companion",
        ReplaceWith("JsonPath.defaultCompiler"),
    )
    var compiler: JsonPathCompiler
        get() = JsonPath.defaultCompiler
        set(value) {
            JsonPath.defaultCompiler = value
        }
}