package com.lanrhyme.shardlauncher.ui.home

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.lanrhyme.shardlauncher.R
import com.lanrhyme.shardlauncher.game.account.Account
import com.lanrhyme.shardlauncher.game.account.getDisplayName
import com.lanrhyme.shardlauncher.ui.components.LocalCardLayoutConfig
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeEffect

@Composable
fun HomeAccountCard(account: Account) {
    val (isCardBlurEnabled, cardAlpha, hazeState) = LocalCardLayoutConfig.current
    val cardWidth = 120.dp
    val cardHeight = 160.dp
    val cardModifier =
        if (isCardBlurEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Modifier.width(cardWidth).height(cardHeight).hazeEffect(state = hazeState)
        } else {
            Modifier.width(cardWidth).height(cardHeight)
        }
    Card(
            modifier = cardModifier,
            shape = RoundedCornerShape(16.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
                ),
    ) {
        Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            AsyncImage(
                    model =
                            ImageRequest.Builder(LocalContext.current)
                                    .data(
                                            "https://mineskin.eu/helm/${account.username}"
                                    )
                                    .error(R.drawable.img_steve)
                                    .crossfade(true)
                                    .diskCachePolicy(CachePolicy.ENABLED)
                                    .memoryCachePolicy(CachePolicy.ENABLED)
                                    .build(),
                    contentDescription = "Account Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().weight(3f)
            )

            // Info Section
            Column(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .weight(1f)
                                    .padding(horizontal = 2.dp, vertical = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly // Distribute text evenly
            ) {
                Text(
                        text = account.username,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                )
                Text(
                        text = account.getDisplayName(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                )
            }
        }
    }
}
