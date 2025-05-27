package at.asitplus.jsonpath

import at.asitplus.jsonpath.core.JsonPathCompiler

@Deprecated("Moved to JsonPath.Companion.")
object JsonPathDependencyManager {
    /**
     * Function extension repository that may be extended with custom functions by the user of this library.
     */
    @Deprecated("Moved to JsonPath.Companion.", ReplaceWith("JsonPath.defaultFunctionExtensionRepository"))
    var functionExtensionRepository: JsonPathFunctionExtensionRepository
        get() = JsonPath.defaultFunctionExtensionRepository
        set(value) {
            JsonPath.defaultFunctionExtensionRepository = value
        }

    @Deprecated("Moved to JsonPath.Companion.", ReplaceWith("JsonPath.defaultCompiler"))
    var compiler: JsonPathCompiler
        get() = JsonPath.defaultCompiler
        set(value) {
            JsonPath.defaultCompiler = value
        }
}

