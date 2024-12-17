package com.example.rockit.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.rockit.R
import com.example.rockit.Utils.Utils.getArtistName
import com.example.rockit.Utils.Utils.parseText
import com.example.rockit.models.PlaybackState
import com.example.rockit.ui.theme.Black
import com.example.rockit.ui.theme.Green
import com.example.rockit.ui.theme.White
import com.example.rockit.ui.viewmodels.BaseViewModel
import com.example.rockit.ui.viewmodels.UIState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.coroutines.EmptyCoroutineContext

@Composable
fun SongScreen(
    viewModel: BaseViewModel,
    navController: NavController
) {

    val uiState by viewModel.uiState.collectAsState()

    val currentProgress by viewModel.currentProgress.collectAsState()

    val currentPlayList by viewModel.currentPlayList.collectAsState()

    val currentSongIndex by viewModel.currentSongIndex.collectAsState()

    val configuration = LocalConfiguration.current

    var imageSize by rememberSaveable {
        mutableIntStateOf(250)
    }

    var buttonSize by rememberSaveable {
        mutableIntStateOf(48)
    }

    val orientation by remember {
        mutableIntStateOf(configuration.orientation)
    }

    LaunchedEffect(orientation) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            imageSize = 125
            buttonSize = 24
        } else {
            imageSize = 250
            buttonSize = 48
        }
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(color = Black)
            .padding(10.dp)
            .fillMaxSize()
    ) {

        CircularImage(
            imageSize = imageSize,
            imageData = currentSongIndex?.let {
                currentPlayList?.get(
                    it
                )?.image?.get(2)?.url
            } ?: R.drawable.music_backup_image,
            uiState = uiState
        )

        Text(
            text = currentSongIndex?.let {
                currentPlayList?.get(it)?.name?.let { songName ->
                    parseText(songName)
                }
            } ?: "Unknown",
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp,
            color = White,
            modifier = Modifier
                .padding(
                    top = 10.dp,
                    bottom = 5.dp
                )
                .basicMarquee(iterations = Int.MAX_VALUE)
        )

        Text(
            text = currentSongIndex?.let { getArtistName(currentPlayList?.get(it)?.artists?.primary) } ?: "Unknown",
            fontFamily = FontFamily.SansSerif,
            fontSize = 24.sp,
            color = White,
            modifier = Modifier
                .padding(
                    top = 5.dp,
                    bottom = 10.dp
                )
                .basicMarquee(iterations = Int.MAX_VALUE)
        )

        SliderLayout(
            currentSliderPosition = currentProgress,
            valueRange = 0f .. (currentSongIndex?.let { currentPlayList?.get(it)?.duration?.toFloat() }
                ?: 0f),
            onSliderChanged = { position ->
                viewModel.jumpToTimeStamp(position)
            }
        )

        Row(
            modifier = Modifier
                .padding(
                    top = 10.dp,
                    start = 20.dp,
                    end = 20.dp
                )
        ) {

            MusicButtons(
                modifier = Modifier
                    .padding(end = 3.dp)
                    .weight(1f),
                drawableRes = R.drawable.previous,
                contentDescription = "previous button",
                onclick = {
                    viewModel.playPrevious()
                },
                buttonSize = buttonSize
            )

            if (uiState == UIState.Playing) {

                MusicButtons(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 3.dp,
                            end = 3.dp
                        )
                        .weight(1f),
                    drawableRes = R.drawable.pause,
                    contentDescription = "pause button",
                    onclick = {
                        viewModel.playOrPauseSong()
                    },
                    buttonSize = buttonSize
                )

            } else if (uiState == UIState.Buffering) {

                CircularProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 3.dp,
                            end = 3.dp
                        )
                        .heightIn(min = 100.dp)
                        .size(48.dp)
                        .weight(1f),
                    color = White
                )

            } else {

                MusicButtons(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 3.dp,
                            end = 3.dp
                        )
                        .weight(1f),
                    drawableRes = R.drawable.play,
                    contentDescription = "play button",
                    onclick = {
                        viewModel.playOrPauseSong()
                    },
                    buttonSize = buttonSize
                )

            }

            MusicButtons(
                modifier = Modifier
                    .padding(start = 3.dp)
                    .weight(1f),
                drawableRes = R.drawable.next,
                contentDescription = "next button",
                onclick = {
                    viewModel.playNext()
                },
                buttonSize = buttonSize
            )

        }

    }


}

@Composable
fun MusicButtons(
    modifier : Modifier,
    drawableRes : Int,
    contentDescription: String,
    onclick : () -> Unit,
    buttonSize : Int
) {
    Image(
        painter = painterResource(drawableRes),
        contentDescription = contentDescription,
        colorFilter = ColorFilter.tint(Color.Black),
        modifier = modifier
            .padding(
                start = 3.dp
            )
            .heightIn(min = if (buttonSize == 48) 100.dp else Dp.Unspecified)
            .size(buttonSize.dp)
            .clip(
                RoundedCornerShape(5.dp)
            )
            .background(White)
            .clickable {
                onclick()
            }
    )
}

@Composable
fun CircularImage(
    imageSize : Int,
    imageData : Any,
    uiState: UIState
) {

    var currentRotation by remember { mutableFloatStateOf(0f) }

    val rotation = remember { Animatable(currentRotation) }

    LaunchedEffect(uiState) {
        if (uiState == UIState.Playing) {
            rotation.animateTo(
                targetValue = currentRotation + 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(40000, easing = LinearEasing)
                )
            ) {
                currentRotation = value
            }
        }
    }

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).crossfade(true).data(imageData).build(),
        placeholder = painterResource(R.drawable.music_backup_image),
        contentDescription = "music image",
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .padding(
                bottom = 10.dp
            )
            .rotate(rotation.value)
            .size(imageSize.dp)
            .clip(CircleShape)
            .border(
                width = 2.dp,
                color = Green,
                shape = CircleShape
            )
    )
}

