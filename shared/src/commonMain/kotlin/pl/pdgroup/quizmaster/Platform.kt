package pl.pdgroup.quizmaster

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
