package com.lanrhyme.shardlauncher.ui.home

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lanrhyme.shardlauncher.game.account.Account
import com.lanrhyme.shardlauncher.game.account.getDisplayName

@Composable
fun HomeAccountCard(account: Account) {
    val cardWidth = 120.dp
    val cardHeight = 160.dp
    Card(
            modifier = Modifier.width(cardWidth).height(cardHeight),
            shape = RoundedCornerShape(22.dp),
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
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
                                            "https://api.xingzhige.com/API/get_Minecraft_skins/?name=${account.username}&type=avatar&overlay=true"
                                    )
                                    .crossfade(true)
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
