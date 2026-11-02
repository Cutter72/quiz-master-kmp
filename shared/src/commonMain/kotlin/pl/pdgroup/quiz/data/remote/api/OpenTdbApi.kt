package pl.pdgroup.quiz.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import pl.pdgroup.quiz.data.remote.model.OpenTdbResponse

interface OpenTdbApi {
    suspend fun getQuestions(
        amount: Int,
        category: Int,
        difficulty: String,
        type: String = "multiple"
    ): OpenTdbResponse
}

class OpenTdbApiImpl(
    private val client: HttpClient
) : OpenTdbApi {
    override suspend fun getQuestions(
        amount: Int,
        category: Int,
        difficulty: String,
        type: String
    ): OpenTdbResponse {
        return client.get("api.php") {
            parameter("amount", amount)
            parameter("category", category)
            parameter("difficulty", difficulty)
            parameter("type", type)
        }.body()
    }
}