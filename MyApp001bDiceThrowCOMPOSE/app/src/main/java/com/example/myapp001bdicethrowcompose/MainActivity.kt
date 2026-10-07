package com.example.myapp001bdicethrowcompose

import android.graphics.drawable.Animatable as AnimatableDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Unicode dice faces, index 0 = value 1 ... index 5 = value 6
private val diceSymbols = listOf("⚀", "⚁", "⚂", "⚃", "⚄", "⚅")

// Songs + gifs and loss images + texts are paired: the same index = one pair (same lists as the XML app)
private val spinGifs = listOf(R.raw.dancing_cat, R.raw.dance, R.raw.rave, R.raw.rave2, R.raw.ad67ish)
private val songs = listOf(R.raw.kostka_song, R.raw.song2, R.raw.mlg_song, R.raw.pivo_song, R.raw.memory_song)
private val lossImages = listOf(R.drawable.mr_kot, R.drawable.pepe, R.drawable.thiccomniman, R.drawable.husbantu)
private val lossTexts = listOf(R.string.drafted_text, R.string.pepe, R.string.omni, R.string.husbantu)

// Number of random changes before the final result and the time of one change (one pulse)
private const val SHUFFLE_COUNT = 25
private const val SHUFFLE_DELAY_MS = 250

// Pulse sizes: each change starts small and grows; every next change grows a bit bigger
private const val PULSE_START_SCALE = 0.2f
private const val PULSE_FIRST_END_SCALE = 0.8f
private const val PULSE_GROWTH_PER_STEP = 0.18f

// Colours used by the screens (same as the XML app)
private val backgroundColor = Color(0xFFF5F3FF)
private val primaryColor = Color(0xFF352060)

class MainActivity : ComponentActivity() {

    // SQLite database where every final roll is saved
    private lateinit var dbHelper: ResultsDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = ResultsDbHelper(this)

        // Draw behind the system bars; safeDrawingPadding() below keeps content uncovered
        enableEdgeToEdge()

        // The whole UI is described in Kotlin, no XML layout
        setContent {
            MaterialTheme {
                DiceApp(dbHelper)
            }
        }
    }

    override fun onDestroy() {
        // Close the database
        dbHelper.close()
        super.onDestroy()
    }
}

/**
 * App root: switches between the dice screen and the results page.
 * A simple state variable decides which screen is shown (in XML this is a second Activity).
 */
@Composable
fun DiceApp(dbHelper: ResultsDbHelper) {
    var showResults by rememberSaveable { mutableStateOf(false) }

    if (showResults) {
        // System back button returns to the dice screen instead of closing the app
        BackHandler { showResults = false }
        ResultsScreen(dbHelper = dbHelper, onBack = { showResults = false })
    } else {
        DiceScreen(dbHelper = dbHelper, onOpenResults = { showResults = true })
    }
}

/**
 * Main screen: title, large dice, "Hodit" button and the bet outcome, centred on the screen,
 * with a burger button in the top-left corner that opens the side panel.
 * "Hodit" asks for a bet first; while rolling, the dice is replaced by a random GIF (with its song)
 * and a pulsing random number. A lost bet shows a random image + text dialog.
 */
@Composable
fun DiceScreen(dbHelper: ResultsDbHelper, onOpenResults: () -> Unit) {
    // State: when these values change, Compose redraws (recomposes) the affected UI automatically.
    // diceValue uses rememberSaveable so it survives screen rotation (like onSaveInstanceState in XML).
    var diceValue by rememberSaveable { mutableIntStateOf(1) }
    var isRolling by remember { mutableStateOf(false) }
    var spinNumber by remember { mutableIntStateOf(1) }
    var spinIndex by remember { mutableIntStateOf(0) }
    var showBetDialog by remember { mutableStateOf(false) }
    var lossIndex by remember { mutableStateOf<Int?>(null) } // null = "you lost" dialog hidden
    var outcomeText by remember { mutableStateOf("") }

    // Scale of the pulsing number (animated from small to big on every change)
    val pulseScale = remember { Animatable(PULSE_START_SCALE) }

    // State of the side panel (open / closed); it can also be swiped in from the left edge
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    // Coroutine scope bound to this composable, used for the animation and the drawer
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // While the side panel is open, the system back button closes it
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    /**
     * Rolls the dice: plays a random GIF + song and shows 25 random numbers, each pulsing
     * from small to big over 250 ms (every next one bigger). Then shows the final result,
     * saves it with the bet to the database and shows the win / lose outcome.
     */
    fun rollDice(bet: Int) {
        scope.launch {
            outcomeText = ""

            // Pick a random GIF + song pair, then switch the dice area to the GIF
            spinIndex = spinGifs.indices.random()
            isRolling = true

            // Animation: random numbers, each one pulses and ends bigger than the previous one
            repeat(SHUFFLE_COUNT) { step ->
                spinNumber = (1..6).random()
                pulseScale.snapTo(PULSE_START_SCALE)
                pulseScale.animateTo(
                    targetValue = PULSE_FIRST_END_SCALE + step * PULSE_GROWTH_PER_STEP,
                    animationSpec = tween(durationMillis = SHUFFLE_DELAY_MS, easing = LinearOutSlowInEasing)
                )
            }

            // Final result: changing the state is enough, Compose shows the new dice by itself
            val rolledValue = (1..6).random()
            diceValue = rolledValue
            isRolling = false

            // Save the result and the bet to SQLite on a background thread
            withContext(Dispatchers.IO) { dbHelper.insertResult(rolledValue, bet) }

            // Show the outcome of the bet
            if (rolledValue == bet) {
                outcomeText = context.getString(R.string.outcome_win, bet, rolledValue)
            } else {
                outcomeText = context.getString(R.string.outcome_lose, bet, rolledValue)
                lossIndex = lossImages.indices.random()
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // Side panel: title and the "Výsledky" button
            ModalDrawerSheet(drawerContainerColor = Color.White) {
                Column(modifier = Modifier.safeDrawingPadding().padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.menu),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            // Close the panel, then open the results page
                            scope.launch { drawerState.close() }
                            onOpenResults()
                        }
                    ) {
                        Text(stringResource(R.string.results))
                    }
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .safeDrawingPadding() // keeps content away from status and navigation bars
        ) {
            // Title, dice area, button and outcome in the centre
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Title "Hoď kostkou"
                Text(
                    text = stringResource(R.string.title),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )

                // Dice area: either the dice symbol (idle) or the GIF with a pulsing number (rolling)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isRolling) {
                        SpinningGif(
                            gifRes = spinGifs[spinIndex],
                            songRes = songs[spinIndex],
                            modifier = Modifier.fillMaxSize()
                        )
                        // Random number on top of the GIF, pulses from small to big on every change
                        Text(
                            text = spinNumber.toString(),
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            style = TextStyle(shadow = Shadow(color = Color.Black, blurRadius = 12f)),
                            modifier = Modifier.graphicsLayer {
                                scaleX = pulseScale.value
                                scaleY = pulseScale.value
                            }
                        )
                    } else {
                        // Large dice symbol, read from state
                        Text(
                            text = diceSymbols[diceValue - 1],
                            fontSize = 160.sp,
                            color = primaryColor
                        )
                    }
                }

                // Button that starts the roll (asks for the bet first), disabled while rolling
                Button(
                    enabled = !isRolling,
                    onClick = { showBetDialog = true }
                ) {
                    Text(text = stringResource(R.string.roll), fontSize = 24.sp)
                }

                // Result of the bet (win / lose), empty before the first roll
                Text(
                    text = outcomeText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            // Burger button in the top-left corner, opens the side panel
            IconButton(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .size(56.dp),
                onClick = { scope.launch { drawerState.open() } }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_menu),
                    contentDescription = stringResource(R.string.open_menu),
                    tint = primaryColor
                )
            }
        }
    }

    // Bet dialog: choosing a number 1-6 starts the roll with that bet
    if (showBetDialog) {
        BetDialog(
            onBet = { bet ->
                showBetDialog = false
                rollDice(bet)
            },
            onDismiss = { showBetDialog = false }
        )
    }

    // "You lost" dialog with a random image + text pair
    lossIndex?.let { index ->
        DraftedDialog(
            imageRes = lossImages[index],
            textRes = lossTexts[index],
            onDismiss = { lossIndex = null }
        )
    }
}

/**
 * Animated GIF / WebP from res/raw, shown with a classic ImageView inside Compose (AndroidView).
 * While it is on screen, its paired song plays from the beginning (looping); both stop when it disappears.
 */
@Composable
fun SpinningGif(@RawRes gifRes: Int, @RawRes songRes: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val drawable = remember(gifRes) { loadAnimatedImage(context, gifRes) }

    // Start the animation and the song when shown; stop and free them when removed from the screen
    DisposableEffect(drawable, songRes) {
        (drawable as? AnimatableDrawable)?.start()
        val songPlayer = MediaPlayer.create(context, songRes).apply {
            isLooping = true // in case the roll is longer than the song
            start()
        }
        onDispose {
            (drawable as? AnimatableDrawable)?.stop()
            songPlayer.release()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { viewContext ->
            ImageView(viewContext).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                contentDescription = viewContext.getString(R.string.spin_image)
            }
        },
        update = { imageView -> imageView.setImageDrawable(drawable) }
    )
}

/** Dialog with numbers 1-6 as a list (like AlertDialog.setItems in XML); choosing one starts the roll. */
@Composable
fun BetDialog(onBet: (Int) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.bet_title)) },
        text = {
            Column {
                for (number in 1..6) {
                    Text(
                        text = "${diceSymbols[number - 1]}  $number",
                        fontSize = 18.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBet(number) }
                            .padding(vertical = 12.dp)
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

/** "You lost" dialog: image with black bold text under it (same as dialog_drafted.xml). */
@Composable
fun DraftedDialog(@DrawableRes imageRes: Int, @StringRes textRes: Int, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(imageRes),
                contentDescription = stringResource(R.string.drafted_image),
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = stringResource(textRes),
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp)
            )
            TextButton(
                modifier = Modifier.align(Alignment.End),
                onClick = onDismiss
            ) { Text(stringResource(R.string.ok)) }
        }
    }
}

/** Results page: reads all saved rolls from SQLite and shows them in a scrollable list. */
@Composable
fun ResultsScreen(dbHelper: ResultsDbHelper, onBack: () -> Unit) {
    // null = still loading, otherwise the rows from the database
    var results by remember { mutableStateOf<List<GameResult>?>(null) }
    val dateFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()) }

    // Load the rows once, on a background thread
    LaunchedEffect(Unit) {
        results = withContext(Dispatchers.IO) { dbHelper.getAllResults() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .safeDrawingPadding()
    ) {
        Text(
            text = stringResource(R.string.results_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor,
            modifier = Modifier.padding(16.dp)
        )

        // Shown only when the database has no rows yet
        val rows = results
        if (rows != null && rows.isEmpty()) {
            Text(
                text = stringResource(R.string.results_empty),
                fontSize = 18.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // One row per saved roll: "#id   ⚃ 4   sázka 2 ✗   07.10.2026 13:30:12"
        LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            items(rows.orEmpty(), key = { it.id }) { item ->
                Text(
                    text = stringResource(
                        R.string.result_row,
                        item.id,
                        diceSymbols[item.result - 1],
                        item.result,
                        item.bet,
                        if (item.isWin) "✓" else "✗",
                        dateFormat.format(Date(item.timestamp))
                    ),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                HorizontalDivider()
            }
        }

        // Back button returns to the dice screen
        Button(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp),
            onClick = onBack
        ) {
            Text(stringResource(R.string.back))
        }
    }
}
