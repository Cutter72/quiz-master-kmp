package pl.pdgroup.quizmaster

import android.app.Application
import org.koin.android.ext.koin.androidContext
import pl.pdgroup.quizmaster.di.initKoin

class QuizMasterApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@QuizMasterApplication)
        }
    }
}
