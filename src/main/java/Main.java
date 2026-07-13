import java.util.*;
import static java.lang.System.out;

public class Main {
    void main() throws InterruptedException {
        out.println("Enter help for summary.\nEnter start for start");
        Scanner input = new Scanner(System.in);
        String command = input.nextLine();

        if (command.equals("help")) {
            printSummary();
            main();
        } else if (command.equals("start")) {
            out.println("Enter names of labels in format: \"name, name2, name3\".");
            String inputLine = input.nextLine();
            String[] names = inputLine.split("\\s*,\\s*");
            Map<String, String> labels = new HashMap<>();
            Map<String, Integer> labelCount = new HashMap<>();

            for (String name : names) {
                out.println("Enter regex for name " + name + " in format: \"GEN@GEN.ru\". GEN is generated value.");
                labels.put(name, input.nextLine());
            }

            for (String name : names) {
                out.println("Enter average length for name " + name + " in format: \"5\".");
                labelCount.put(name, input.nextInt());
            }

            out.println("Enter count of entities for each name.");
            long entityCount = input.nextLong();

            out.println("Starting!");

            for (String name : names) {
                Set<MainWorker> workers = new HashSet<>();
                out.println("\nЗначения для имени " + name + ":\n");
                for (long i = 0; i < entityCount; i++) {
                    MainWorker mainWorker = new MainWorker(labels.get(name), labelCount.get(name));
                    workers.add(mainWorker);
                    mainWorker.start();
                }
                for (MainWorker worker : workers) {
                    worker.join();
                }
            }
        } else {
            out.println("Unknown command");
            printSummary();
            System.exit(1);
        }
    }

    private void printSummary() {
        out.println("========================================================================");
        out.println("      DATA GENERATOR ENGINE v1.0 — REFERENCE GUIDE");
        out.println("========================================================================");
        out.println("This program is designed for high-performance generation of random");
        out.println("textual data (names, emails, IDs, hashes) based on a user-defined text template.\n");
        out.println("CONTROLS:");
        out.println("  1. Enter a list of names (columns/entities) separated by commas.");
        out.println("  2. For each name, specify a generation mask (format).");
        out.println("  3. Specify the average length (the generator will randomly vary it within ±3).");
        out.println("  4. Specify the number of rows to generate.\n");
        out.println("MASK DEFINITION RULES:");
        out.println("  The keyword 'GEN' serves as a marker for inserting random characters.");
        out.println("  Everything written around 'GEN' remains unchanged (static text).\n");
        out.println("Mask examples:");
        out.println("  - GEN@GEN.ru          ->  randomString@randomString.ru");
        out.println("  - id_GEN_2026         ->  id_randomString_2026");
        out.println("  - +7(999)GEN-GEN-GEN  ->  phone number with random letter blocks\n");
        out.println("ARCHITECTURE AND MULTITHREADING:");
        out.println("  Generation for each entity occurs in parallel in isolated");
        out.println("  OS kernel threads. Threads are synchronized via wait barriers (Thread.join),");
        out.println("  which guarantees ordered output with no race conditions.");
        out.println("========================================================================");
    }
}