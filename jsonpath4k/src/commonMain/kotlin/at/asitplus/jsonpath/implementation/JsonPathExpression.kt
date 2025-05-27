package at.asitplus.jsonpath.implementation

import at.asitplus.jsonpath.core.JsonPathFilterExpressionType
import at.asitplus.jsonpath.core.JsonPathFilterExpressionValue
import at.asitplus.jsonpath.core.JsonPathQuery
import at.asitplus.jsonpath.core.JsonPathSelector


internal sealed interface JsonPathExpression {

    data object NoType : JsonPathExpression

    data object ErrorType : JsonPathExpression

    data class SelectorExpression(val selector: JsonPathSelector) : JsonPathExpression

    sealed interface FilterExpression : JsonPathExpression {
        val expressionType: JsonPathFilterExpressionType

        operator fun invoke(context: JsonPathExpressionEvaluationContext): JsonPathFilterExpressionValue

        fun interface ValueExpression : FilterExpression {
            override val expressionType
                get() = JsonPathFilterExpressionType.ValueType

            override fun invoke(context: JsonPathExpressionEvaluationContext): JsonPathFilterExpressionValue.ValueTypeValue
        }

        fun interface LogicalExpression : FilterExpression {
            override val expressionType
                get() = JsonPathFilterExpressionType.LogicalType

            override fun invoke(context: JsonPathExpressionEvaluationContext): JsonPathFilterExpressionValue.LogicalTypeValue
        }

        sealed interface NodesExpression : FilterExpression {
            override val expressionType
                get() = JsonPathFilterExpressionType.NodesType

            override fun invoke(context: JsonPathExpressionEvaluationContext): JsonPathFilterExpressionValue.NodesTypeValue

            sealed interface FilterQueryExpression : NodesExpression {
                val jsonPathQuery: JsonPathQuery

                override fun invoke(context: JsonPathExpressionEvaluationContext): JsonPathFilterExpressionValue.NodesTypeValue.FilterQueryResult {
                    val nodeList = jsonPathQuery.invoke(
                        currentNode = context.currentNode,
                        rootNode = context.rootNode,
                    ).map {
                        it.value
                    }
                    return JsonPathFilterExpressionValue.NodesTypeValue.FilterQueryResult.NonSingularQueryResult(
                        nodeList
                    )
                }

                data class SingularQueryExpression(
                    override val jsonPathQuery: JsonPathQuery,
                ) : FilterQueryExpression {
                    fun toValueTypeValue(): ValueExpression {
                        return ValueExpression { context ->
                            invoke(context).nodeList.firstOrNull()?.let {
                                JsonPathFilterExpressionValue.ValueTypeValue.JsonValue(it)
                            } ?: JsonPathFilterExpressionValue.ValueTypeValue.Nothing
                        }
                    }

                    override fun invoke(context: JsonPathExpressionEvaluationContext): JsonPathFilterExpressionValue.NodesTypeValue.FilterQueryResult.SingularQueryResult {
                        return JsonPathFilterExpressionValue.NodesTypeValue.FilterQueryResult.SingularQueryResult(
                            super.invoke(context).nodeList
                        )
                    }
                }

                data class NonSingularQueryExpression(
                    override val jsonPathQuery: JsonPathQuery,
                ) : FilterQueryExpression {
                    override fun invoke(context: JsonPathExpressionEvaluationContext): JsonPathFilterExpressionValue.NodesTypeValue.FilterQueryResult.NonSingularQueryResult {
                        return JsonPathFilterExpressionValue.NodesTypeValue.FilterQueryResult.NonSingularQueryResult(
                            super.invoke(context).nodeList
                        )
                    }
                }
            }

            fun interface NodesFunctionExpression : NodesExpression {
                override fun invoke(context: JsonPathExpressionEvaluationContext): JsonPathFilterExpressionValue.NodesTypeValue.FunctionExtensionResult
            }
        }
    }
}