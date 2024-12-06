package com.example.rockit.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.rockit.R
import com.example.rockit.Utils.Utils
import com.example.rockit.Utils.Utils.parseText
import com.example.rockit.models.Song
import com.example.rockit.ui.theme.Black
import com.example.rockit.ui.theme.Gray
import com.example.rockit.ui.theme.Green
import com.example.rockit.ui.theme.LightGray
import com.example.rockit.ui.theme.White

@Composable
fun SongItem(
    song : Song,
    onClick : () -> Unit,
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 10.dp,
                bottom = 10.dp,
                start = 10.dp,
                end = 10.dp
            )
            .clip(RoundedCornerShape(10.dp))
            .background(LightGray)
            .padding(
                5.dp
            ).clickable {
                onClick()
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).crossfade(true)
                .data(song.image?.get(1)?.url)
                .build(),
            placeholder = painterResource(R.drawable.music_backup_image),
            contentDescription = "music image",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .padding(
                    end = 5.dp
                )
                .size(70.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp,
                    color = Green,
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .padding(
                    start = 5.dp,
                )
                .weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = parseText(song.name),
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Black,
                modifier = Modifier
                    .padding( bottom = 1.dp)
                    .basicMarquee(iterations = Int.MAX_VALUE)
            )

            Text(
                text = song.artists?.primary?.let { Utils.getArtistName(it) } ?: "",
                fontFamily = FontFamily.SansSerif,
                fontSize = 16.sp,
                color = Black,
                modifier = Modifier
                    .padding(top = 1.dp)
                    .basicMarquee(iterations = Int.MAX_VALUE)
            )

        }
    }

}