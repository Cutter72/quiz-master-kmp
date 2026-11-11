package pl.pdgroup.quizmaster.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Immutable
sealed interface UiText {
    @Immutable
    data class DynamicString(val value: String) : UiText

    @Immutable
    data class Resource(
        val res: StringResource,
        val args: List<Any> = emptyList()
    ) : UiText

    @Composable
    fun asString(): String {
        return when (this) {
            is DynamicString -> value
            is Resource -> {
                val resolvedArgs = args.map { arg ->
                    when (arg) {
                        is UiText -> arg.asString()
                        is StringResource -> stringResource(arg)
                        else -> arg
                    }
                }.toTypedArray()
                stringResource(res, *resolvedArgs)
            }
        }
    }

    suspend fun asStringSuspend(): String {
        return when (this) {
            is DynamicString -> value
            is Resource -> {
                val resolvedArgs = args.map { arg ->
                    when (arg) {
                        is UiText -> arg.asStringSuspend()
                        is StringResource -> getString(arg)
                        else -> arg
                    }
                }.toTypedArray()
                getString(res, *resolvedArgs)
            }
        }
    }
}
