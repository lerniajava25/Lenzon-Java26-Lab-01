package se.lernia.elpris;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;

public class Main {
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        String elomrade = "";

        while (running){
            System.out.println("""
        Elpriser – Analysverktyg  [%s]
        ========================
        1. Välj elområde (SE1, SE2, SE3, SE4)
        2. Min, Max och Medelpris
        3. Sortera priser (lägst till högst)
        4. Bästa laddningstid (4h sammanhängande)
        e. Avsluta""".formatted(elomrade));


            System.out.print("Ditt val: ");

            if (!scanner.hasNextLine()) {
                System.out.println();
                System.out.println("Ingen mer inmatning. Avslutar.");
                break;
            }

            String val = scanner.nextLine().trim().toLowerCase();

            switch (val) {
                case "1" -> {
                    System.out.print("Ange elområde (SE1, SE2, SE3, SE4): ");

                    if (!scanner.hasNextLine()) {
                        System.out.println();
                        System.out.println("Ingen mer inmatning. Avslutar.");
                        running = false;
                    } else {
                        String nyttOmrade = scanner.nextLine().trim().toUpperCase();

                        String[] giltiga = {"SE1", "SE2", "SE3", "SE4"};
                        boolean hittad = false;

                        for (String omrade : giltiga) {
                            if (omrade.equals(nyttOmrade)) {
                                hittad = true;
                            }
                        }

                        if (hittad) {
                            elomrade = nyttOmrade;
                            System.out.println("Valt område: " + elomrade);
                        } else {
                            System.out.println("Ogiltigt elområde: " + nyttOmrade + ". Välj SE1-SE4.");
                        }
                    }
                }
                case "2" -> {
                    if (elomrade.isEmpty()) {
                        System.out.println("Välj elområde först (menyval 1).");
                    } else {
                        try {
                            double[] timpriser = hamtaTimpriser(elomrade);
                            visaStatistik(timpriser);
                        } catch (IOException e) {
                            System.out.println("Kunde inte hämta priser: " + e.getMessage());
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            System.out.println("Hämtningen avbröts.");
                        } catch (IllegalStateException e) {
                            System.out.println("Ogiltig data från API:et: " + e.getMessage());
                        }
                    }
                }

                case "3" -> {
                    if (elomrade.isEmpty()) {
                        System.out.println("Välj elområde först (menyval 1).");
                    } else {
                        try {
                            double[] timpriser = hamtaTimpriser(elomrade);
                            visaSorterat(timpriser);
                        } catch (IOException e) {
                            System.out.println("Kunde inte hämta priser: " + e.getMessage());
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            System.out.println("Hämtningen avbröts.");
                        } catch (IllegalStateException e) {
                            System.out.println("Ogiltig data från API:et: " + e.getMessage());
                        }
                    }
                }
                case "4" -> {
                    if (elomrade.isEmpty()) {
                        System.out.println("Välj elområde först (menyval 1).");
                    } else {
                        try {
                            double[] timpriser = hamtaTimpriser(elomrade);
                            visaBastaLaddningstid(timpriser);
                        } catch (IOException e) {
                            System.out.println("Kunde inte hämta priser: " + e.getMessage());
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            System.out.println("Hämtningen avbröts.");
                        } catch (IllegalStateException e) {
                            System.out.println("Ogiltig data från API:et: " + e.getMessage());
                        }
                    }
                }

                case "e" -> {
                    System.out.println("Systemet avslutat, på återseende!");
                    running = false;
                }
                default -> System.out.println("Ogiltigt val: \"" + val + "\". Välj 1-4 eller e.");
            }
        }
    }
    private static String hamtaJson(String elomrade) throws IOException, InterruptedException {
        LocalDate idag = LocalDate.now();

        Path cacheFil = Path.of("cache", idag + "_" + elomrade + ".json");   // ← ditt svar 1 ✅

        if (Files.exists(cacheFil)) {                                        // ← rättat svar 2
            System.out.println("(läser från cache)");
            return Files.readString(cacheFil);                               // ← rättat svar 3: HÄR, först
        }

        String datum = "%d/%02d-%02d".formatted(idag.getYear(), idag.getMonthValue(), idag.getDayOfMonth());
        String url = "https://www.elprisetjustnu.se/api/v1/prices/" + datum + "_" + elomrade + ".json";

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Servern svarade " + response.statusCode() + " för område " + elomrade);
        }

        Files.createDirectories(cacheFil.getParent());
        Files.writeString(cacheFil, response.body());                        // ← ditt svar 4 ✅
        System.out.println("(hämtar från API)");

        return response.body();
    }

    private static double[] tillTimpriser(Elpris[] priser) {
        if (priser == null || priser.length < 24) {
            int antal = (priser == null) ? 0 : priser.length;
            throw new IllegalStateException("för få prisposter (" + antal + ", minst 24 krävs)");
        }

        int perTimme = priser.length / 24;
        double[] timpriser = new double[24];

        for (int timme = 0; timme < 24; timme++) {
            double summa = 0;

            for (int i = 0; i < perTimme; i++) {
                summa = summa + priser[timme * perTimme + i].sekPerKwh();
            }

            timpriser[timme] = summa / perTimme;
        }

        return timpriser;
    }

    private static double[] hamtaTimpriser(String elomrade) throws IOException, InterruptedException {
        String json = hamtaJson(elomrade);
        ObjectMapper mapper = new ObjectMapper();
        Elpris[] priser = mapper.readValue(json, Elpris[].class);
        return tillTimpriser(priser);
    }

    private static void visaStatistik(double[] timpriser) {
        double lagsta = timpriser[0];
        double hogsta = timpriser[0];
        double summa = 0;
        int billigastTimme = 0;
        int dyrastTimme = 0;

        for (int t = 0; t < timpriser.length; t++) {
            if (timpriser[t] < lagsta) {
                lagsta = timpriser[t];
                billigastTimme = t;
            }
            if (timpriser[t] > hogsta) {
                hogsta = timpriser[t];
                dyrastTimme = t;
            }
            summa = summa + timpriser[t];
        }

        double medel = summa / timpriser.length;

        System.out.println("Lägsta pris:  %6.2f öre/kWh  kl %02d:00".formatted(lagsta * 100, billigastTimme));
        System.out.println("Högsta pris:  %6.2f öre/kWh  kl %02d:00".formatted(hogsta * 100, dyrastTimme));
        System.out.println("Medelpris:    %6.2f öre/kWh".formatted(medel * 100));
    }

    private static void visaSorterat(double[] timpriser) {
        Timpris[] lista = new Timpris[timpriser.length];

        for (int t = 0; t < timpriser.length; t++) {
            lista[t] = new Timpris(t, timpriser[t] * 100);
        }

        Arrays.sort(lista, (a, b) -> Double.compare(a.ore(), b.ore()));

        System.out.println("Timmar sorterade billigast till dyrast:");
        for (Timpris tp : lista) {
            System.out.println("kl %02d:00  %6.2f öre/kWh".formatted(tp.timme(), tp.ore()));
        }
    }

    private static void visaBastaLaddningstid(double[] timpriser) {
        int fonster = 4;
        double basta = Double.MAX_VALUE;
        int bastaStart = 0;

        for (int start = 0; start <= timpriser.length - fonster; start++) {
            double summa = 0;

            for (int i = 0; i < fonster; i++) {
                summa = summa + timpriser[start + i];
            }

            double medel = summa / fonster;

            if (medel < basta) {
                basta = medel;
                bastaStart = start;
            }
        }

        int slut = (bastaStart + fonster) % 24;

        System.out.println("Bästa laddningstid: kl %02d:00 – %02d:00".formatted(bastaStart, slut));
        System.out.println("Snittpris:          %6.2f öre/kWh".formatted(basta * 100));
    }

}
