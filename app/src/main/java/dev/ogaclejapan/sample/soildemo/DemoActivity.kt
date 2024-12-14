package dev.ogaclejapan.sample.soildemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import soil.query.SwrClient
import soil.query.compose.SwrClientProvider
import soil.query.queryClient
import javax.inject.Inject

@AndroidEntryPoint
class DemoActivity : ComponentActivity() {

    @Inject
    lateinit var swrClient: SwrClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // rememberQueryを利用するには必ず上位コンポーネントでSwrClientProviderの宣言が必要です
            SwrClientProvider(client = swrClient) {
                MaterialTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            var count by rememberSaveable { mutableIntStateOf(3) }
                            DemoList(count)
                            DemoCounterControls(
                                onIncrement = { count++ },
                                isIncrementEnabled = count < 10,
                                onDecrement = { count-- },
                                isDecrementEnabled = count > 0,
                                onRefresh = {
                                    swrClient.effect {
                                        queryClient.invalidateQueriesBy(DemoQueryKey.Id)
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DemoList(
    count: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(count) { i ->
            key(i) { Demo() }
        }
    }
}

@Composable
fun DemoCounterControls(
    onIncrement: () -> Unit,
    isIncrementEnabled: Boolean,
    onDecrement: () -> Unit,
    isDecrementEnabled: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        FilledIconButton(
            onClick = onIncrement,
            enabled = isIncrementEnabled
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_add_24px),
                contentDescription = "Add"
            )
        }
        FilledIconButton(
            onClick = onDecrement,
            enabled = isDecrementEnabled
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_remove_24px),
                contentDescription = "Remove"
            )
        }
        FilledIconButton(
            onClick = onRefresh
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_refresh_24px),
                contentDescription = "Refresh"
            )
        }
    }
}
