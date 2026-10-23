package com.example.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    private val openFoodFactsClient: OkHttpClient by lazy {
        val userAgentInterceptor = Interceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "ZenFast-OpenKitchen-Android/1.3 (contact@zenfast.org)")
                .header("Accept", "application/json")
                .build()
            chain.proceed(request)
        }

        OkHttpClient.Builder()
            .addInterceptor(userAgentInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val theMealDbClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    val openFoodFactsApi: OpenFoodFactsApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.org/")
            .client(openFoodFactsClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(OpenFoodFactsApi::class.java)
    }

    val theMealDbApi: TheMealDbApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://www.themealdb.com/")
            .client(theMealDbClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(TheMealDbApi::class.java)
    }
}
