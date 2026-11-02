package pl.pdgroup.quizmaster

import android.app.Application
import org.koin.android.ext.koin.androidContext
import pl.pdgroup.quiz.di.initKoin

class QuizApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@QuizApplication)
        }
    }
}
