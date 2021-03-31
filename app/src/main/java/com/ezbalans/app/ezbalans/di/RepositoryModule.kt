package com.ezbalans.app.ezbalans.di

import com.ezbalans.app.ezbalans.repository.DatabaseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun providesDatabaseRepository(): DatabaseRepository{
        return DatabaseRepository()
    }
}