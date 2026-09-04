package se.lernia.elpris;
import java.util.Scanner;

public class Main {
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running){
            System.out.println("""
        Elpriser – Analysverktyg
        ========================
        1. Välj elområde (SE1, SE2, SE3, SE4)
        2. Min, Max och Medelpris
        3. Sortera priser (lägst till högst)
        4. Bästa laddningstid (4h sammanhängande)
        e. Avsluta""");


            System.out.print("Ditt val: ");
            String val = scanner.nextLine().trim().toLowerCase();

            switch (val) {
                case "1" -> System.out.println(">> Välj elområde – inte klart än.");
                case "2" -> System.out.println("Min, max och medelpris");
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
}



