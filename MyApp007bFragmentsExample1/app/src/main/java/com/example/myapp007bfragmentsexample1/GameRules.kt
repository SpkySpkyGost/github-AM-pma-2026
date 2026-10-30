package com.example.myapp007bfragmentsexample1

enum class HandSign(val imageDrawableResourceId: Int) {
    STONE(R.drawable.stone),
    SCISSORS(R.drawable.scissors),
    PAPER(R.drawable.paper);

    fun beats(opponentHandSign: HandSign): Boolean = when (this) {
        STONE -> opponentHandSign == SCISSORS
        SCISSORS -> opponentHandSign == PAPER
        PAPER -> opponentHandSign == STONE
    }
}

enum class RoundOutcome {
    PLAYER_WINS,
    APP_WINS,
    DRAW;

    companion object {
        fun determineRoundOutcome(playerHandSign: HandSign, appHandSign: HandSign): RoundOutcome = when {
            playerHandSign == appHandSign -> DRAW
            playerHandSign.beats(appHandSign) -> PLAYER_WINS
            else -> APP_WINS
        }
    }
}
