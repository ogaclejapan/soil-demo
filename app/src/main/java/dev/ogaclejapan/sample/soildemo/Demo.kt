package dev.ogaclejapan.sample.soildemo

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import soil.query.QueryState
import soil.query.compose.QueryLoadingErrorObject
import soil.query.compose.QueryLoadingObject
import soil.query.compose.QueryRefreshErrorObject
import soil.query.compose.QuerySuccessObject
import soil.query.compose.SwrClientProvider
import soil.query.compose.rememberQuery
import soil.query.compose.tooling.QueryPreviewClient
import soil.query.compose.tooling.SwrPreviewClient
import timber.log.Timber

@Composable
fun Demo(modifier: Modifier = Modifier) {
    val coroutineScope = rememberCoroutineScope()
    // 少なくともロジックとビューのコンポーネントは分けるべきですが、デモコードなので簡略化しています
    val query = rememberQuery(key = DemoQueryKey())
    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SideEffect {
            Timber.i("DEMO/Recomposition: $query")
        }
        when (query) {
            is QuerySuccessObject -> Text(
                "✨ ${query.data.stargazersCount}",
                modifier = Modifier.testTag("result")
            )

            is QueryLoadingObject -> Text("Loading...", modifier = Modifier.testTag("result"))
            is QueryLoadingErrorObject,
            is QueryRefreshErrorObject -> Text("Error :(", modifier = Modifier.testTag("result"))
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = {
            coroutineScope.launch { query.refresh() }
        }, enabled = !query.isAwaited()) {
            Text("Refresh")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DemoPreview() {
    val swrClient = remember {
        SwrPreviewClient(
            query = QueryPreviewClient {
                on(DemoQueryKey.Id) {
                    QueryState.success(Repo(9999))
                }
            }
        )
    }
    SwrClientProvider(client = swrClient) {
        MaterialTheme {
            Demo()
        }
    }
}
