Responsibilities (what we each made)
Nathan
* Graphic assets (backgrounds, buttons, logos etc.)
* Stock “algorithm” 
* Stock graphics
* Trading screen
* Notification system
* Saving feature
Jerry
* Stock list UI
* PumpBuddy AI chatbot[a] + text box implementation
* Fine-tuning and debugging UI
Collaborative
* Stock parameters
* Stock testing tool
* General design decisions


Functionalities missing
* “Quotas”/missions 
   * We felt that having profit goals doesn’t really add much to the game; the objective is to make as much money as possible regardless
* No-fail mode
   * No quotas means we don’t need to implement nofail


Functionalities added
* Notification system
* Chatbot
* Saving
* Medium-term trends through the means of target-flipping
   * In the beginning the plan was for all stocks to hover at around the same price indefinitely
________________


Known issues/bugs
* No wordwrap on the chatbot inputs AND outputs
* Chatbot accepts invalid amounts for buy/sell (negative amounts)
   * I have however done my best to make sure these orders do not actually fill
* Notifications can sometimes block the chatbot
   * As a band-aid, only 1 notification will ever stack on stock list
   * In retrospection notifications should probably have been implemented as a deque to better support this but I didn’t want to change the code and spend 2 hours fixing a ghost error


Extra info
* Feel free to test out the bounds of the chatbot! It should work with reasonable enough natural language for buying and selling
* If you would like to start with a ton of money, there is an option to do so when starting a new save
* The logic itself for stock generation can be found in Stock.java in the nextCandlestick() and generateCandlestick() methods
* I have included a formatting guide txt file in gameFiles to explain the format for the various text files if necessary


In-class concepts in our code
At least 2 list/set: ArrayList stocks in driver, Queue notifications in notification.java, HashSet buy/sellOrder in Stock.java


Comparable interface implemented in Stock, Comparator implemented in SortByVolatility.java


Min. 4 classes:
PumpAndDumpGame.java
Stock.java
Candlestick.java
Player.java
Chatbot.java
…etc
[a]I did not touch this part of the program AT ALL, it was 100% Jerry