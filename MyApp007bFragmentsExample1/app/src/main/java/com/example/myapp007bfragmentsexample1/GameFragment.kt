package com.example.myapp007bfragmentsexample1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withStarted
import com.example.myapp007bfragmentsexample1.databinding.FragmentGameBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameFragment : Fragment() {

    private var _gameFragmentBinding: FragmentGameBinding? = null
    private val gameFragmentBinding get() = _gameFragmentBinding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _gameFragmentBinding = FragmentGameBinding.inflate(inflater, container, false)
        return gameFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gameFragmentBinding.buttonPlayStone.setOnClickListener { playRound(HandSign.STONE) }
        gameFragmentBinding.buttonPlayScissors.setOnClickListener { playRound(HandSign.SCISSORS) }
        gameFragmentBinding.buttonPlayPaper.setOnClickListener { playRound(HandSign.PAPER) }
    }

    private fun playRound(playerHandSign: HandSign) {
        setPlayerHandSignButtonsEnabled(false)
        gameFragmentBinding.roundStatusText.text = getString(R.string.app_is_choosing)

        // Coroutine is tied to the fragment's view, so it stops automatically if the view is destroyed.
        viewLifecycleOwner.lifecycleScope.launch {
            showShufflingHandSignImages()

            val appHandSign = HandSign.entries.random()
            gameFragmentBinding.appHandSignImage.setImageResource(appHandSign.imageDrawableResourceId)
            delay(APP_HAND_SIGN_REVEAL_DURATION_MILLIS)

            when (RoundOutcome.determineRoundOutcome(playerHandSign, appHandSign)) {
                RoundOutcome.DRAW -> {
                    gameFragmentBinding.roundStatusText.text = getString(R.string.draw_pick_again)
                    setPlayerHandSignButtonsEnabled(true)
                }
                // withStarted waits until the app is in the foreground, so switching fragments never crashes in the background.
                RoundOutcome.PLAYER_WINS -> viewLifecycleOwner.lifecycle.withStarted {
                    replaceCurrentScreenWith(VictoryFragment())
                }
                RoundOutcome.APP_WINS -> viewLifecycleOwner.lifecycle.withStarted {
                    replaceCurrentScreenWith(LostFragment())
                }
            }
        }
    }

    private suspend fun showShufflingHandSignImages() {
        val numberOfShuffleFrames = (SHUFFLE_ANIMATION_DURATION_MILLIS / IMAGE_SWITCH_INTERVAL_MILLIS).toInt()
        repeat(numberOfShuffleFrames) { shuffleFrameIndex ->
            val handSignShownInThisFrame = HandSign.entries[shuffleFrameIndex % HandSign.entries.size]
            gameFragmentBinding.appHandSignImage.setImageResource(handSignShownInThisFrame.imageDrawableResourceId)
            delay(IMAGE_SWITCH_INTERVAL_MILLIS)
        }
    }

    private fun setPlayerHandSignButtonsEnabled(areButtonsEnabled: Boolean) {
        gameFragmentBinding.buttonPlayStone.isEnabled = areButtonsEnabled
        gameFragmentBinding.buttonPlayScissors.isEnabled = areButtonsEnabled
        gameFragmentBinding.buttonPlayPaper.isEnabled = areButtonsEnabled
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _gameFragmentBinding = null
    }

    private companion object {
        const val SHUFFLE_ANIMATION_DURATION_MILLIS = 2000L
        const val IMAGE_SWITCH_INTERVAL_MILLIS = 125L
        const val APP_HAND_SIGN_REVEAL_DURATION_MILLIS = 1000L
    }
}
