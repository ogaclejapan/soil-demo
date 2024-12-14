package dev.ogaclejapan.sample.soildemo

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.IdlingResource
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.runComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.yield
import org.junit.Test
import org.junit.runner.RunWith
import soil.query.SwrCache
import soil.query.SwrCacheScope
import soil.query.compose.SwrClientProvider
import soil.query.test.TestSwrClient
import soil.query.test.test

@ExperimentalTestApi
@RunWith(AndroidJUnit4::class)
class DemoTest {

    @Test
    fun demoSuccess() = runComposeUiTest {
        val client = SwrCache(SwrCacheScope()).test {
            on(DemoQueryKey.Id) {
                // このブロックはDemoQueryKeyに定義したfetchの代わりとして実行されます（それ以外は実際の動作と同じ）
                yield()
                Repo(123)
            }
        }
        registerIdlingResource(client)
        setContent {
            SwrClientProvider(client) {
                Demo()
            }
        }
        onNode(hasText("✨ 123")).assertExists()
    }

    @Test
    fun demoFailure() = runComposeUiTest {
        val client = SwrCache(SwrCacheScope()).test {
            on(DemoQueryKey.Id) {
                yield()
                error("test")
            }
        }
        registerIdlingResource(client)
        setContent {
            SwrClientProvider(client) {
                Demo()
            }
        }
        onNode(hasText("Error :(")).assertExists()
    }

    // RobolectricはIdlingResourceが動かないので、UnitTest上で動かす場合は waitUntil { client.isIdleNow() } を代わりに使う必要があります
    // ref. https://github.com/robolectric/robolectric/issues/4807
    private fun ComposeUiTest.registerIdlingResource(client: TestSwrClient) {
        registerIdlingResource(object : IdlingResource {
            override val isIdleNow: Boolean get() = client.isIdleNow()
        })
    }
}
