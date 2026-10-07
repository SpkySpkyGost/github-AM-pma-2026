package com.example.myapp001adicethrowxml

import android.content.Intent
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    // Unicode dice faces, index 0 = value 1 ... index 5 = value 6
    private val diceSymbols = listOf("⚀", "⚁", "⚂", "⚃", "⚄", "⚅")
    //songs + gifs/text + img are paired. Put the corresponding files on the same list ids/positions
    private val spinGifs = listOf(R.raw.dancing_cat, R.raw.dance, R.raw.rave, R.raw.rave2, R.raw.ad67ish)
    private val songs = listOf(R.raw.kostka_song, R.raw.song2, R.raw.mlg_song, R.raw.pivo_song, R.raw.memory_song)
    private val lossImages = listOf(R.drawable.mr_kot, R.drawable.pepe, R.drawable.thiccomniman)
    private val lossTexts = listOf(R.string.drafted_text, R.string.pepe, R.string.omni)
    // Number of random changes before the final result and the time of one change (one pulse)
    private val shuffleCount = 25
    private val shuffleDelayMs = 250L

    // Pulse sizes: each change starts small and grows; every next change grows a bit bigger
    private val pulseStartScale = 0.2f
    private val pulseFirstEndScale = 0.8f
    private val pulseGrowthPerStep = 0.18f

    // References to views from activity_main.xml (found with findViewById)
    private lateinit var dlMain: DrawerLayout
    private lateinit var tvDice: TextView
    private lateinit var ivSpin: ImageView
    private lateinit var tvSpinNumber: TextView
    private lateinit var tvOutcome: TextView
    private lateinit var btnRoll: Button

    // SQLite database where every final roll is saved
    private lateinit var dbHelper: ResultsDbHelper

    // Dancing GIF shown while rolling (loaded once)
    private lateinit var spinDrawable: Drawable

    // Song played while the GIF is shown (res/raw/kostka_song.mp3)
    private lateinit var songPlayer: MediaPlayer

    // Current value on the dice (1-6), kept so it survives screen rotation
    private var diceValue = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Draw behind the system bars, then load the XML layout
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        dbHelper = ResultsDbHelper(this)

        // Find the views by their IDs
        dlMain = findViewById(R.id.dlMain)
        tvDice = findViewById(R.id.tvDice)
        ivSpin = findViewById(R.id.ivSpin)
        tvSpinNumber = findViewById(R.id.tvSpinNumber)
        tvOutcome = findViewById(R.id.tvOutcome)
        btnRoll = findViewById(R.id.btnRoll)
        val flContent = findViewById<FrameLayout>(R.id.flContent)
        val llDrawer = findViewById<LinearLayout>(R.id.llDrawer)
        val ibMenu = findViewById<ImageButton>(R.id.ibMenu)
        val btnResults = findViewById<Button>(R.id.btnResults)

        // Load the animated GIF (WebP) from res/raw and put it into the ImageView
        spinDrawable = loadAnimatedImage(this, R.raw.dancing_cat)
        ivSpin.setImageDrawable(spinDrawable)

        // Prepare the song; it loops in case the roll is longer than the song
        songPlayer = MediaPlayer.create(this, R.raw.kostka_song)
        songPlayer.isLooping = true

        // Add padding equal to the status/navigation bar size so neither the content
        // nor the side panel is covered by the system bars (panel keeps its own 16dp padding too)
        val drawerPadding = (16 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(dlMain) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            flContent.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            llDrawer.setPadding(
                systemBars.left + drawerPadding,
                systemBars.top + drawerPadding,
                drawerPadding,
                systemBars.bottom + drawerPadding
            )
            insets
        }

        // Restore the last rolled value after rotation
        diceValue = savedInstanceState?.getInt(KEY_DICE_VALUE) ?: 1
        showDice(diceValue)

        // "Hodit" first asks for the bet, the roll starts after a number is chosen
        btnRoll.setOnClickListener { showBetDialog() }

        // Burger button opens the side panel (it can also be swiped in from the left edge)
        ibMenu.setOnClickListener { dlMain.openDrawer(GravityCompat.START) }

        // "Výsledky" in the side panel closes the panel and opens the results page
        btnResults.setOnClickListener {
            dlMain.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, ResultsActivity::class.java))
        }

        // While the side panel is open, the system back button closes it instead of the app
        val closeDrawerOnBack = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                dlMain.closeDrawer(GravityCompat.START)
            }
        }
        onBackPressedDispatcher.addCallback(this, closeDrawerOnBack)
        dlMain.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) { closeDrawerOnBack.isEnabled = true }
            override fun onDrawerClosed(drawerView: View) { closeDrawerOnBack.isEnabled = false }
        })
    }

    /** Dialog with numbers 1-6; choosing one starts the roll with that bet. */
    private fun showBetDialog() {
        val options = Array(6) { index -> "${diceSymbols[index]}  ${index + 1}" }
        AlertDialog.Builder(this)
            .setTitle(R.string.bet_title)
            .setItems(options) { _, which -> rollDice(bet = which + 1) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * Rolls the dice: hides the dice, plays the GIF and shows 10 random numbers,
     * each pulsing from small to big over 500 ms (every next one bigger).
     * Then shows the final result, saves it with the bet to the database
     * and shows the win / lose outcome. The button is disabled the whole time.
     */
    private fun rollDice(bet: Int) {
        // lifecycleScope runs the coroutine on the main thread and cancels it when the activity is destroyed
        lifecycleScope.launch {
            btnRoll.isEnabled = false
            tvOutcome.text = ""

            // Pick a random GIF + song pair
            val spinIndex = spinGifs.indices.random()
            spinDrawable = loadAnimatedImage(this@MainActivity, spinGifs[spinIndex])
            ivSpin.setImageDrawable(spinDrawable)

            //Free the previous player (if any) and prepare the new song
            if (::songPlayer.isInitialized) songPlayer.release()
            songPlayer = MediaPlayer.create(this@MainActivity, songs[spinIndex])
            songPlayer.isLooping = true
            setSpinning(true)

            // Animation: 10 random numbers, each one pulses and ends bigger than the previous one
            repeat(shuffleCount) { step ->
                tvSpinNumber.text = (1..6).random().toString()
                val endScale = pulseFirstEndScale + step * pulseGrowthPerStep
                tvSpinNumber.scaleX = pulseStartScale
                tvSpinNumber.scaleY = pulseStartScale
                tvSpinNumber.animate()
                    .scaleX(endScale)
                    .scaleY(endScale)
                    .setDuration(shuffleDelayMs)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
                delay(shuffleDelayMs)
            }

            // Final result: back to the normal dice view
            setSpinning(false)
            diceValue = (1..6).random()
            showDice(diceValue)

            // Save the result and the bet to SQLite on a background thread
            val rolledValue = diceValue
            withContext(Dispatchers.IO) { dbHelper.insertResult(rolledValue, bet) }

            // Show the outcome of the bet
            if (rolledValue == bet) {
                tvOutcome.text = getString(R.string.outcome_win, bet, rolledValue)
            } else {
                tvOutcome.text = getString(R.string.outcome_lose, bet, rolledValue)
                showDraftedDialog()
            }

            btnRoll.isEnabled = true
        }
    }

    /**
     * Switches the dice area between the dice symbol and the playing GIF with the pulsing number.
     * The song plays from the beginning while the GIF is shown and pauses when it disappears.
     */
    private fun setSpinning(spinning: Boolean) {
        tvDice.visibility = if (spinning) View.INVISIBLE else View.VISIBLE
        ivSpin.visibility = if (spinning) View.VISIBLE else View.GONE
        tvSpinNumber.visibility = if (spinning) View.VISIBLE else View.GONE
        val animatable = spinDrawable as? Animatable
        if (spinning) animatable?.start() else animatable?.stop()
        if (spinning) {
            songPlayer.seekTo(0)
            songPlayer.start()
        } else if (songPlayer.isPlaying) {
            songPlayer.pause()
        }
    }

    /** "You lost" dialog: Mr. Kot image with black bold text under it (layout dialog_drafted.xml). */
    private fun showDraftedDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_drafted, null)

        // Pick a random image + text pair and put it into the dialog's views
        val lossIndex = lossImages.indices.random()
        dialogView.findViewById<ImageView>(R.id.ivDrafted).setImageResource(lossImages[lossIndex])
        dialogView.findViewById<TextView>(R.id.tvDrafted).setText(lossTexts[lossIndex])

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    /** Imperative UI update: we set the new text on the TextView ourselves. */
    private fun showDice(value: Int) {
        tvDice.text = diceSymbols[value - 1]
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_DICE_VALUE, diceValue)
    }

    override fun onDestroy() {
        // Free the audio player and close the database
        songPlayer.release()
        dbHelper.close()
        super.onDestroy()
    }

    companion object {
        private const val KEY_DICE_VALUE = "dice_value"
    }
}
