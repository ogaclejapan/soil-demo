package dev.ogaclejapan.sample.soildemo

import androidx.compose.runtime.Immutable
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import soil.query.QueryId
import soil.query.QueryKey
import soil.query.core.KeyEquals
import soil.query.receivers.ktor.buildKtorQueryKey

// Soilでは、Hooks(rememberQuery)へ直接インラインで書く方法は推奨していません。（TanStack QueryのQuery Optionsと同じ思想です）
@Immutable
class DemoQueryKey : KeyEquals(), QueryKey<Repo> by buildKtorQueryKey(
    id = Id,
    // buildKtorQueryKey は Ktor 用の QueryKey を生成するためのインライン関数です。
    // SwrCachePolicyで渡したhttpClientプロパティは、このレシーバ型に渡ります。
    fetch = { // HttpClient.() -> Repo
        get("https://api.github.com/repos/soil-kt/soil").body()
    }
) {
    // キャッシュの一意性はQueryIdの比較で保証します。
    // 専用のId型を必ずしも用意する必要はありませんが、副作用の実行やプレビュー／テストなどで役立つ場面があります。
    object Id : QueryId<Repo>("github-repo/soil")
}

@Serializable
data class Repo(
    @SerialName("stargazers_count")
    val stargazersCount: Int
)
