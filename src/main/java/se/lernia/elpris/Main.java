package se.lernia.elpris;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Scanner;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
            String val = scanner.nextLine().trim().toLowerCase();

            switch (val) {
                case "1" -> {
                    System.out.print("Ange elområde (SE1, SE2, SE3, SE4): ");
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
                case "2" -> {
                    if (elomrade.isEmpty()) {
                        System.out.println("Välj elområde först (menyval 1).");
                    } else {
                        try {
                            double[] timpriser = hamtaTimpriser(elomrade);
                            visaStatistik(timpriser);
                        } catch (IOException | InterruptedException e) {
                            System.out.println("Kunde inte hämta priser: " + e.getMessage());
                        }
                    }
                }

                case "3" -> System.out.println("Sortera priser: ");
                case "4" -> System.out.println(" Bästa laddningstid:");

                case "e" -> {
                    System.out.println("Systemet avslutat, på återseende!");
                    running = false;
                }
                default -> System.out.println("Ogiltigt val: \"" + val + "\". Välj 1-4 eller e.");
            }
        }
    }
    private static String hamtaJson(String elomrade) throws IOException, InterruptedException {
        String url = "https://www.elprisetjustnu.se/api/v1/prices/2026/09-04_" + elomrade + ".json";

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Servern svarade " + response.statusCode() + " för område " + elomrade);
        }

        return response.body();
    }

    private static double[] tillTimpriser(Elpris[] priser) {
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

}



