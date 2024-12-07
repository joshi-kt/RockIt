package com.example.rockit.ui.screens

import android.media.AudioTimestamp
import android.media.Image
import android.media.MediaDescription
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.rockit.R
import com.example.rockit.Utils.Utils.convertTimestampToString
import com.example.rockit.Utils.Utils.getArtistName
import com.example.rockit.models.PlaybackState
import com.example.rockit.models.Song
import com.example.rockit.ui.theme.Black
import com.example.rockit.ui.theme.Gray
import com.example.rockit.ui.theme.Green
import com.example.rockit.ui.theme.White
import com.example.rockit.ui.viewmodels.BaseViewModel
import com.example.rockit.ui.viewmodels.UIState
import kotlinx.coroutines.Job

@Composable
fun AllSongsScreen(
    viewModel: BaseViewModel
) {

    var searchText by rememberSaveable {
        mutableStateOf("")
    }

    val uiState by viewModel.uiState.collectAsState()

    val isFetching by viewModel.isFetching.collectAsState()

    val currentProgress by viewModel.currentProgress.collectAsState()

    val currentPlayList by viewModel.currentPlayList.collectAsState()

    val visibleSongs by viewModel.visibleSongs.collectAsState()

    val currentSongIndex by viewModel.currentSongIndex.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Black)
            .padding(5.dp)
    ) {

        TextField(
            value = searchText,

            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    all = 5.dp
                ),
            placeholder = {
                Text(
                    text = "Search here ...",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 16.sp,
                    color = Black,
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = White,
                unfocusedContainerColor = White,
                focusedIndicatorColor = Green
            ),
            textStyle = TextStyle(
                color = Black,
                fontSize = 18.sp,
            ),
            onValueChange = {
                searchText = it
                viewModel.searchSongs(searchText)
            })

        if (isFetching) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(count = 20, itemContent = {
                    ShimmerItem()
                })
            }

        } else {

            if (visibleSongs.isNullOrEmpty()) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "OPPS ! No song found",
                        textAlign = TextAlign.Center,
                        color = Color.White,
                        fontSize = 25.sp
                    )
                }

            } else {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    itemsIndexed(items = visibleSongs!!,
                        key = { index, item -> item.id },
                        itemContent = { index, item ->
                            SongItem(
                                song = item,
                                onClick = {
                                    viewModel.playSong(index)
                                })
                        })
                }

            }

        }

        BottomBar(
            uiState = uiState,
            currentSliderPosition = currentProgress,
            valueRange = 0f .. (currentSongIndex?.let { currentPlayList?.get(it)?.duration?.toFloat() }
                ?: 0f),
            currentPlayList = currentPlayList,
            selectedSongIndex = currentSongIndex,
            onSliderChanged = { position ->
                viewModel.jumpToTimeStamp(position)
            },
            onMusicButtonPressed = {
                when(it) {
                    PlaybackState.RESUME -> {
                        viewModel.playOrPauseSong()
                    }
                    PlaybackState.PAUSE -> {
                        viewModel.playOrPauseSong()
                    }
                    PlaybackState.NEXT -> {
                        viewModel.playNext()
                    }
                    PlaybackState.PREVIOUS -> {
                        viewModel.playPrevious()
                    }
                }
            }
        )

    }

}


@Composable
fun BottomBar(
    uiState: UIState,
    currentSliderPosition : Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onSliderChanged : (Float) -> Unit,
    onMusicButtonPressed : (PlaybackState) -> Unit,
    currentPlayList : List<Song>?,
    selectedSongIndex : Int?,
) {

    Column(
        modifier = Modifier
            .padding(
                top = 2.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
        ) {

        SliderLayout(
            currentSliderPosition = currentSliderPosition,
            valueRange = valueRange,
            onSliderChanged = {
                onSliderChanged(it)
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    bottom = 2.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .crossfade(true)
                    .data( selectedSongIndex?.let { currentPlayList?.get(selectedSongIndex)?.image?.get(2)?.url } ?: LocalContext.current.getDrawable(R.drawable.music_backup_image) )
                    .build(),
                placeholder = painterResource(R.drawable.music_backup_image),
                contentDescription = "music image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .padding(
                        end = 5.dp
                    )
                    .size(65.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = Green,
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ){

                Text(
                    text = selectedSongIndex?.let { currentPlayList?.get(selectedSongIndex)?.name} ?: "Unknown",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = White,
                    modifier = Modifier
                        .padding(
                            bottom = 1.dp
                        )
                        .basicMarquee(iterations = Int.MAX_VALUE)
                )

                Text(
                    text = selectedSongIndex?.let { getArtistName(currentPlayList?.get(selectedSongIndex)?.artists?.primary) } ?: "Unknown",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 16.sp,
                    color = White,
                    modifier = Modifier
                        .padding(
                            top = 1.dp
                        )
                        .basicMarquee(iterations = Int.MAX_VALUE)
                )

            }

            MusicControlButtons(
                modifier = Modifier.padding(
                    end = 3.dp
                ),
                drawableRes = R.drawable.previous,
                contentDescription = "previous button",
                onclick = {
                    onMusicButtonPressed(PlaybackState.PREVIOUS)
                }
            )

            if (uiState == UIState.Playing) {

                MusicControlButtons(
                    modifier = Modifier.padding(
                        start = 3.dp,
                        end = 3.dp
                    ),
                    drawableRes = R.drawable.pause,
                    contentDescription = "pause button",
                    onclick = {
                        onMusicButtonPressed(PlaybackState.PAUSE)
                    }
                )

            } else if (uiState == UIState.Buffering) {

                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp),
                    color = White
                )

            } else {

                MusicControlButtons(
                    modifier = Modifier.padding(
                        start = 3.dp,
                        end = 3.dp
                    ),
                    drawableRes = R.drawable.play,
                    contentDescription = "play button",
                    onclick = {
                        onMusicButtonPressed(PlaybackState.RESUME)
                    }
                )

            }

            MusicControlButtons(
                modifier = Modifier.padding(
                    start = 3.dp,
                ),
                drawableRes = R.drawable.next,
                contentDescription = "next button",
                onclick = {
                    onMusicButtonPressed(PlaybackState.NEXT)
                }
            )

        }

    }

}

@Composable
fun MusicControlButtons(
    modifier : Modifier,
    drawableRes : Int,
    contentDescription: String,
    onclick : () -> Unit,
) {
    Image(
        painter = painterResource(drawableRes),
        contentDescription = contentDescription,
        colorFilter = ColorFilter.tint(Color.Black),
        modifier = modifier
            .padding(
                start = 3.dp
            )
            .size(48.dp)
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
fun SliderLayout(
    currentSliderPosition: Float,
    onSliderChanged: (Float) -> Unit,
    valueRange : ClosedFloatingPointRange<Float>?
) {

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {

        Text(
            text = convertTimestampToString(currentSliderPosition),
            fontFamily = FontFamily.SansSerif,
            fontSize = 16.sp,
            color = White,
            modifier = Modifier.padding(
                end = 2.dp
            )
        )

        Slider(
            value = currentSliderPosition,
            valueRange = valueRange ?:  0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = Green,
                activeTrackColor = Green,
            ),
            modifier = Modifier
                .padding(
                    start = 2.dp,
                    end = 2.dp,
                )
                .weight(1f),
            onValueChange = {
                onSliderChanged(it)
            })

        Text(
            text = valueRange?.endInclusive?.let { convertTimestampToString(it) } ?: "00:00",
            fontFamily = FontFamily.SansSerif,
            fontSize = 16.sp,
            color = White,
            modifier = Modifier.padding(
                start = 2.dp
            )
        )

    }

}