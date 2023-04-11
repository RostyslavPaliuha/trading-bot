ECHO Kill previous trading bot process
taskkill /FI "WindowTitle eq trading_bot" /T /F
ECHO Start trading bot process
start "trading_bot" java -jar E:\development\projects\trading-bot\target\trading-bot-0.2.jar