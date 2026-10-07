# github-tz-pma-2026
Pro účely předmětu PMA - verze 2026.

## Seznam cvičení

2026-10-01 - Hod kostkou

## Porovnání: jak každá varianta aktualizuje zobrazenou kostku

XML (imperativní přístup). Vzhled je popsán v activity_main.xml. V MainActivity si pomocí findViewById(R.id.tvDice) najdeme konkrétní TextView a při každé změně mu sami nastavíme nový text: tvDice.text = diceSymbols[value - 1]. Stejně ručně přepínáme btnRoll.isEnabled a viditelnost (visibility) kostky, GIFu a čísla. Když view zapomeneme aktualizovat, obrazovka zůstane stará. Hodnotu kostky po otočení obrazovky obnovujeme sami přes onSaveInstanceState.

Jetpack Compose (deklarativní přístup). Rozhraní je funkce DiceScreen() v Kotlinu. Hodnota kostky je stav var diceValue by rememberSaveable { mutableIntStateOf(1) }. Při hodu měníme jen tento stav (diceValue = rolledValue); Compose změnu zaznamená a sám znovu vykreslí (rekompozice) Text, který z diceValue čte. Tlačítko je napojené na stav isRolling přes enabled = !isRolling a stejný stav rozhoduje, jestli se kreslí kostka, nebo GIF (if (isRolling) ... else ...). Na žádné view se neodkazujeme a rememberSaveable hodnotu udrží i po otočení obrazovky.

V obou variantách běží animace v korutině (lifecycleScope.launch / rememberCoroutineScope), takže se neblokuje hlavní vlákno.

## Vylepšení (obě aplikace se chovají stejně)
Sázka: po stisku „Hodit“ hráč v dialogu vsadí na číslo 1–6.
Animace hodu: místo kostky se přehrává náhodně vybraný animovaný GIF/WebP z res/raw a k němu hraje jeho písnička (MediaPlayer). Přes GIF se 25× (po 250 ms) objeví náhodné číslo, které „pulzuje“ z malé velikosti do velké; každé další naroste víc.
Výsledek: zobrazí se výsledná kostka a text výhry / prohry. Při prohře se ukáže dialog s náhodně vybraným obrázkem a textem.
Ukládání (SQLite): každý hod se uloží do databáze dice_results.db, tabulka results (id, result, bet, timestamp), přes ResultsDbHelper (SQLiteOpenHelper), mimo hlavní vlákno (Dispatchers.IO).
Boční menu: tlačítko ☰ vlevo nahoře otevře panel s tlačítkem „Výsledky“ → seznam uložených hodů (nejnovější nahoře).

Páry GIF + písnička a obrázek + text jsou ve stejných seznamech v obou aplikacích (spinGifs / songs, lossImages / lossTexts), stejný index = jeden pár.
