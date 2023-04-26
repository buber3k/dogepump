# dogepump

W społeczności kryptowalutowej istnieją tzw. grupy sygnałowe. Za odpowiednią kwotę płaconą miesięcznie dostaje się dostęp do grupy (przeważnie server discord lub telegram), w której jej twórcy próbują edukować członków, możliwa jest tam wymiana opinii o inwestycjach itp. Jednak najważniejszą częścią są kanały, na których wydawane są sygnały zakupu kryptowalut na dźwigni. Przeważnie są to krótkie wiadomości zawierające ticker tokena, tzw. entry czyli zasięg ceny, w której powinno się wchodzić oraz tp czyli take profit - cenę, w której pozycję powinno się zamnąć. Jedną z takich grup jest [WWG](https://twitter.com/WalshWealthWWG). Szczególnie tokeny z mniejszą kapitalizacją rynkową potrafią skoczyć o 1-2% w góre po samej wiadomości na takim kanale jeżeli mam on wystarczającą liczbę subskrybentów.

Dogepump to aplikacja napisana w języku Scala, która wykorzystuje fakt istnienia takich właśnie kanałów. Automatyzuje ona proces inwestowania w kryptowaluty na podstawie wiadomości z Discorda. Projekt integruje się z platformą Binance oraz komunikatorami Discord i Telegram, aby zapewnić automatyczne zarządzanie inwestycjami w kryptowaluty.

# Funkcje

1. Automatyczne wykonywanie zleceń zakupu kryptowalut na podstawie wiadomości z określonych kanałów Discorda. <br>
2. Zarządzanie pozycjami zakupionymi z użyciem zdefiniowanych parametrów, takich jak oczekiwany zysk, stop loss i dźwignia finansowa. <br>
3. Monitorowanie pozycji zakupionych, zmiana poziomu stop loss i anulowanie stop loss, gdy nastąpią odpowiednie warunki. <br>
4. Wysyłanie powiadomień o zakupach i zmianach pozycji za pomocą Telegrama. <br>
5. Czarna lista słów i niechcianych monet, aby uniknąć fałszywych sygnałów i powtórnych zakupów. <br>

# Jak to działa
1. Dogepump pobiera informacje konfiguracyjne, takie jak tokeny dostępu dla Discorda i Telegrama oraz inne ustawienia związane z oczekiwanymi zyskami, stop loss i dźwignią finansową.
2. Inicjalizuje klienta Discorda i Telegrama oraz tworzy usługi dla strumienia Discord oraz wymiany Binance.
3. Tworzy czarną listę niepożądanych monet oraz czarną listę słów. Dlaczego jest to ważne? Tokeny z top 10 kapitalizacji rynkowej ciężko ruszyć wiadomością na takim kanale. Ponad to często pojawiają się tzw. follow up wiadomości zawierające informację w stylu <br>
`STX stops moved to BE`<br>
Taka wiadomość oznacza, że ci którzy wcześniej podążyli za sygnałem zakupu kryptowaluty STX powinni przenieść stop loss czyli daną cenę tokena, która w przypadku spadku zamknie pozycję na tzw. break even - czyli cenę otwarcia pozycji. Jest to częsta praktyka gdy po jakimś czasie od otworzenia pozycji cena wzrośnie. Co jednak najważnieje osoby, które ręcznie otwierają pozycję na podstawie takich sygnałów w tym przypadku tego nie zrobią - gdyż nie jest to sygnał zakupu, a tylko takie chcemy wykonywać za pomocą dogepumpa. 
4. Jeżeli token nie znajduję się w top 10 według kapitalizacji rynkowej oraz wiadomość nie zawiera słowa z czarnej listy to aplikacja inicjalizuje usługi zakupu z dźwignią na giełdzie Binance. <br>
5.Na końcu wysyła wiadmość podsumowującą na komunikatorze Telegram.

# Konfiguracja

W pliku config/application znajdują się indywidualne wartości użytkownika
`binance.apiKey oraz binance.secretKey` klucze dostępu API do konta Binance <br>

`telegram.chatId oraz telegram.botToken` ID chatu na telegranie oraz [token Bota](https://medium.com/geekculture/generate-telegram-token-for-bot-api-d26faf9bf064) <br>

`discord.token` - token kanału Discord <br>

`usdCost` wartość pozycji <br>
`leverage` wielkość dźwigni <br>
`expectedProfit` oczekiwany zysk w formacie 1=100% gdzie 100% to cena startowa <br>
`expectedElizProfit` oczekiwany zysk od sygnałów użytkownika Eliz <br>
`expectedJohnProfit` oczekiwany zysk od użytkownika John <br>
`expectedDownPercent` stop loss pozycji w formacie jak wyżej <br>

