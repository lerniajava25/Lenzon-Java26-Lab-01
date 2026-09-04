# Elpris-analysator

Konsolprogram i Java som hämtar dagens elpriser från elprisetjustnu.se och analyserar dem.
Laboration 1, Systemutveckling Java, Lernia.

### Kör

```bash
mvn clean compile
java -cp target/classes se.lernia.elpris.Main
```

Kräver JDK 25 och Maven.

### Om koden

Programmet hämtar dagens spotpriser för valt elområde med Javas inbyggda `HttpClient`.
JSON:en parsas med Jackson till en array av `Elpris`. API:et ger 96 poster i
15-minutersintervall, så de slås ihop fyra och fyra till 24 timpriser innan något räknas ut.
Sedan går det att få min, max och medel, en sorterad lista, eller de fyra billigaste
sammanhängande timmarna. Hämtad JSON sparas i `cache/` med datum och elområde i filnamnet,
så samma dygn hämtas bara en gång.

### Reflektion

Jag har hållit på med JavaScript tidigare, så själva logiken var ingen chock. En loop är en
loop. Det som tog tid var allt runt omkring.

Mest tid la jag på verktygen. Innan jag skrivit en enda rad kod skulle jag installera rätt JDK,
få igång Maven och lära mig IntelliJ. I JS öppnar man en fil och kör den. Här kändes det som
att man måste sätta upp ett halvt maskineri först.

Det andra som bromsade var alla ord. JDK, JVM, bytecode, dependency, Jackson, cache. De
används som om de vore självklara, och man vet inte var man ska börja fråga. Jag fick reda ut
dem ett i taget innan koden gick att förstå.

Roligast var när cachen funkade. Första gången stod det "(hämtar från API)", andra gången
"(läser från cache)" och noll internet. Då kändes det som ett riktigt system och inte en
skoluppgift.

### Källor och tillförlitlighet

Jag använde två saker för att fixa cachningen: en AI och Javadoc, alltså Javas egen
dokumentation.

AI:n var snabb när jag inte fattade vad fil-I/O var eller vilka metoder som fanns. Problemet är
att den kan ha fel utan att man märker det. Jag fick veta att `Files.exists` kastar
`IOException`. Det stämde inte. Jag kollade i Javadoc och testade sen att sätta ett
`catch (IOException)` runt raden — kompilatorn sa nej, alltså kastar den inget.

Javadoc känns mest pålitlig eftersom den kommer från dem som gjort Java, och det står vilken
version den gäller i själva länken (`/javase/25/`). Vanliga guider på nätet har inte det.
Söker man "java write file" hittar man massor av exempel med `new File(...)` som är gammal
Java, men inget på de sidorna säger att de är gamla.

Så jag använde AI för att förstå snabbt och dokumentationen för att kolla att det stämde.

