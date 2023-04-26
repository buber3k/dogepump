# dogepump

W społeczności kryptowalutowej istnieją tzw. grupy sygnałowe. Za odpowiednią kwotę płaconą miesięcznie dostaje się dostęp do grupy (przeważnie server discord lub telegram), w której jej twórcy próbują edukować członków, możliwa jest tam wymiana opinii o inwestycjach itp. Jednak najważniejszą częścią są kanały, na których wydawane są sygnały zakupu kryptowalut na dźwigni. Przeważnie są to krótkie wiadomości zawierające ticker tokena, tzw. entry czyli zasięg ceny, w której powinno się wchodzić oraz tp czyli take profit - cenę, w której pozycję powinno się zamknąć. Jedną z takich grup jest [WWG](https://twitter.com/WalshWealthWWG).

Dogepump to aplikacja napisana w języku Scala, która wykorzystuje fakt istnienia takich właśnie kanałów. Automatyzuje ona proces inwestowania w kryptowaluty na podstawie wiadomości z Discorda. Projekt integruje się z platformą Binance oraz komunikatorami Discord i Telegram, aby zapewnić automatyczne zarządzanie inwestycjami w kryptowaluty.

# funkcje

1.Automatyczne wykonywanie zleceń zakupu kryptowalut na podstawie wiadomości z określonych kanałów Discorda. <br>
2.Zarządzanie pozycjami zakupionymi z użyciem zdefiniowanych parametrów, takich jak oczekiwany zysk, stop loss i dźwignia finansowa. <br>
3.Monitorowanie pozycji zakupionych, zmiana poziomu stop loss i anulowanie stop loss, gdy nastąpią odpowiednie warunki. <br>
4.Wysyłanie powiadomień o zakupach i zmianach pozycji za pomocą Telegrama. <br>
5.Czarna lista słów i niechcianych monet, aby uniknąć fałszywych sygnałów i powtórnych zakupów. <br>
