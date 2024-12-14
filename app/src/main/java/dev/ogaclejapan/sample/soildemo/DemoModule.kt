package dev.ogaclejapan.sample.soildemo

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import soil.query.AndroidMemoryPressure
import soil.query.AndroidNetworkConnectivity
import soil.query.AndroidWindowVisibility
import soil.query.QueryOptions
import soil.query.SwrCache
import soil.query.SwrCachePolicy
import soil.query.SwrCacheScope
import soil.query.SwrClient
import soil.query.receivers.ktor.httpClient
import timber.log.Timber
import java.net.UnknownHostException
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Module
@InstallIn(SingletonComponent::class)
object DemoModule {

    @Singleton
    @Provides
    fun provideKtorHttpClient(): HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
    }

    @Singleton
    @Provides
    fun provideSwrClient(
        @ApplicationContext context: Context,
        ktorClient: HttpClient
    ): SwrClient = SwrCache(
        policy = SwrCachePolicy(
            coroutineScope = SwrCacheScope(),
            queryOptions = QueryOptions(
                // 最後に取得したデータが古い(stale)と見なすまでの時間を指定できます (デフォルトは即staleなので次回のリクエストで再取得されます)
                staleTime = Duration.ZERO,
                // 非アクティブなクエリのキャッシュを破棄するまでの時間を指定できます
                gcTime = 5.minutes,
                // 内部動作デバッグ用途でログメッセージをコールバックします
                logger = { message -> Timber.d("DEMO/Query: $message") },
                // エラーログ送信用途でクエリの例外発生時にエラー内容をコールバックします
                onError = { errorRecord, _ ->
                    Timber.e(
                        errorRecord.exception,
                        "DEMO/Query: failed to request query ${errorRecord.keyId.namespace}!"
                    )
                },
                // リトライ処理の対象とする例外を指定できます (AirPlaneモードでUnknownHostExceptionが再現できます)
                shouldRetry = { err -> err is UnknownHostException }
            ),
            // メモリ逼迫度に応じて非アクティブなクリエのキャッシュを自動的に解放します
            memoryPressure = AndroidMemoryPressure(context),
            // ネットワークのオンライン復帰時に特定条件(networkResumeQueriesFilter)に該当するクエリを再開させることができます（おもにエラー復帰用途）
            networkConnectivity = AndroidNetworkConnectivity(context),
            // バックグランドからフォアグランド復帰時に特定条件(windowVisibilityFilter)に該当するクエリを再開させることができます
            windowVisibility = AndroidWindowVisibility(),
        ) {
            // soil.query.core.ContextReceiver仕様に沿って拡張プロパティを実装することで任意の型を渡すことができます
            httpClient = ktorClient
        }
    )
}
