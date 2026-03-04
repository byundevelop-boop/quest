package com.kurly.android.quest.core.network

/**
 * Network API 클라이언트/Repository 생성을 담당하는 Data 계층 퍼사드.
 */

import android.content.Context
import android.content.pm.ApplicationInfo
import com.kurly.android.mockserver.MockInterceptor
import com.kurly.android.quest.core.domain.repository.MainRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkProvider {

    // 앱에서 사용할 MainRepository를 외부 진입점으로 제공한다.
    fun provideMainRepository(context: Context): MainRepository {
        return createRepository(context.applicationContext)
    }

    // Mock/실서버 통신용 OkHttp + Retrofit 설정으로 MainRepositoryImpl 생성
    internal fun createRepository(context: Context): MainRepository {
        val clientBuilder = OkHttpClient.Builder()
            // 개발용 Mock 응답을 주입한다.
            .addInterceptor(MockInterceptor(context))
            // 네트워크 타임아웃 기본값을 고정한다.
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .callTimeout(20, TimeUnit.SECONDS)

        // 디버그 빌드면 HTTP 요청 로그를 노출한다.
        val isDebuggableApp =
            context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

        if (isDebuggableApp) {
            // 운영에서는 과한 로그를 막고, 디버그에서만 최소 요청 정보 출력
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            clientBuilder.addInterceptor(logging)
        }

        val client = clientBuilder
            .build()

        // 서버 엔드포인트와 JSON 파서를 묶어 API 호출 클라이언트를 만든다.
        val retrofit = Retrofit.Builder()
            .baseUrl("https://kurly.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Retrofit API 인터페이스를 생성해서 Repository 구현체에 주입한다.
        val api = retrofit.create(KurlyApi::class.java)
        return MainRepositoryImpl(api)
    }
}
