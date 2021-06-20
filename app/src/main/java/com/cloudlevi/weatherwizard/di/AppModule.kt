package com.cloudlevi.weatherwizard.di

import android.app.Application
import androidx.room.Room
import com.cloudlevi.weatherwizard.data.WizardDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        app: Application,
        callback: WizardDatabase.Callback

    ) = Room.databaseBuilder(app, WizardDatabase::class.java, "wizard_database")
        .createFromAsset("databases/world_cities.db")
        .addCallback(callback)
        .build()



    @Provides
    fun provideWizardDao(db: WizardDatabase) = db.wizardDao()

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope