package com.example.rockit.di

import android.content.Context
import com.example.rockit.data.network.ApiService
import com.example.rockit.data.network.RetrofitClient
import com.example.rockit.data.repository.ConnectivityRepository
import com.example.rockit.data.repository.DataRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun provideApiService(): ApiService {
        return RetrofitClient.retrofitService
    }

    @Singleton
    @Provides
    fun provideDataRepository(apiService: ApiService): DataRepository {
        return DataRepository(apiService)
    }

    @Singleton
    @Provides
    fun provideConnectivityRepository(
        @ApplicationContext context: Context
    ): ConnectivityRepository = ConnectivityRepository(context)
}