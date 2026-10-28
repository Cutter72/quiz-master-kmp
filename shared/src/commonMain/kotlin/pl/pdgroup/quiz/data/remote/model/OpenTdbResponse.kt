package pl.pdgroup.quiz.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenTdbResponse(
    @SerialName("response_code") val responseCode: Int,
    @SerialName("results") val results: List<QuestionDto>
)