# Pump and Dump Game

A paper-trading / crypto-trading simulator built in Java (Swing/AWT). Start with a set amount of virtual money and try to grow it as much as possible by trading across 12 procedurally-priced stocks and cryptocurrencies — each with its own volatility, growth target, and risk profile.

# Gallery
### Main Menu

### Save Selection

### Stock Menu

### Trading Screen

## Features

- **12 tradable assets** — including index-style stocks (S&P, Nasdaq, Dow Jones), large caps (NVDA, AMZN, WMT, PLTR, BRK.B), and crypto (ETH, SOL, DOGE, LITE)
- **Procedural price simulation** — a parametric algorithm generates realistic-looking candlestick price histories driven by:
  - Mean-reversion pressure
  - Momentum
  - Probability-weighted "target-flipping" for medium-term trend shifts
- **Save system** — create and manage multiple player-specific save slots, with 1,400+ candlesticks saved across the 12 stocks so gameplay can pick up right where you left off
- **In-game trading screen** — buy and sell positions, track portfolio value and lifetime P&L
- **PumpBuddy chatbot** — a lightweight natural-language assistant for placing buy/sell orders directly through chat, using string-similarity matching to interpret input
- **Notification system** — real-time in-game alerts for order fills and events
- **Interactive tutorial** — an in-app slideshow walks new players through the game
- **Hard mode / starting balance options** — choose your starting capital and difficulty when creating a new save

## Tech Stack

- **Language:** Java
- **UI:** Java Swing / AWT
- **Persistence:** Custom flat-file save format (see `formattingGuide.txt`)
- **Tooling:** Eclipse project (`.project` / `.classpath` / `.settings`)

## Getting Started

### Run the prebuilt JAR

```bash
java -jar pumpAndDumpGame.jar
```

### Run from source (Eclipse)

1. Clone the repo and import `pumpAndDumpGame` as an existing Eclipse project.
2. Run `pumpAndDumpGame/PumpAndDumpGame.java` as a Java application.

> The game reads assets and save data relative to the `src/gameFiles/` directory, so run it from the `pumpAndDumpGame/` project root.

## Customizing Stocks

Assets are defined in `src/gameFiles/stockList.txt`, one per line:

```
SYMBOL  initialPrice  volatility  growthTarget  stability  targetFlipChance
```

The generation logic itself lives in `Stock.java`, primarily in the `nextCandlestick()` and `generateCandlestick()` methods. See `src/gameFiles/formattingGuide.txt` for the full data format used across stock, player, and save files.

## Known Issues

- No word-wrap on chatbot input/output text
- The chatbot accepts invalid buy/sell amounts (e.g., negative values), though invalid orders are prevented from actually filling
- Notifications can occasionally block the chatbot; only one notification is allowed to stack on the stock list as a workaround

## Credits

**Nathan:** graphic assets, stock pricing algorithm, stock graphics, trading screen, notification system, save system
**Jerry:** stock list UI, PumpBuddy chatbot + text box implementation, UI fine-tuning and debugging
**Collaborative:** stock parameters, in-house stock testing tool, overall design decisions

## License

No license specified.
