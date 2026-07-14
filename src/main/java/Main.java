import lombok.NonNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import static java.lang.System.out;

public class Main {
    public static void main(String[] args) {
        try {
            out.println("Enter help for summary.\nEnter start for start");
            Scanner input = new Scanner(System.in);
            String command = input.nextLine();

            if (command.equals("help")) {
                printSummary();
                main(new String[0]);
            } else if (command.equals("start")) {
                out.println("Save output to file or print? Answer \"file\" or \"print\"");
                boolean toFile = input.nextLine().equals("file");
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

                if (!toFile) {
                    out.println("Starting!");
                    for (String name : names) {
                        Set<MainWorker> workers = new HashSet<>();
                        out.println("\nMeanings for the name " + name + ":\n");
                        for (long i = 0; i < entityCount; i++) {
                            MainWorker mainWorker = new MainWorker(labels.get(name), labelCount.get(name), true);
                            workers.add(mainWorker);
                            mainWorker.start();
                        }
                        for (MainWorker worker : workers) {
                            worker.join();
                        }
                    }
                }
                else {
                    out.println("Enter path to save the file in format: \"C:\\dir\\output.txt\".");
                    input.nextLine();
                    File file = new File(input.nextLine());
                    if (file.exists()) {
                        out.println("File " + file.getAbsolutePath() + " already exists.");
                        System.exit(2);
                    }
                    else {
                        out.println("Choose saving format:");
                        out.println("  1 - JSON array of objects: [{\"name1\":\"value1\",\"name2\":\"value2\"},...]");
                        out.println("  2 - CSV with header: name1,name2,name3");
                        out.println("  3 - Plain text, one value per line (values separated by comma)");
                        out.println("  4 - Custom format (enter template like: '{\"name\":' )");
                        out.print("Your choice: ");

                        int formatChoice = input.nextInt();
                        input.nextLine();

                        String format;
                        switch (formatChoice) {
                            case 1: format = "json"; break;
                            case 2: format = "csv"; break;
                            case 3: format = "plain"; break;
                            case 4:
                                out.println("Enter custom format template. Use {name} as placeholder for field name and {value} for generated value.");
                                out.println("Example: '{\"name\":\"{value}\"}'");
                                format = input.nextLine();
                                break;
                            default:
                                out.println("Invalid choice, using JSON by default.");
                                format = "json";
                        }

                        generateToFile(names, labels, labelCount, entityCount, file, format);
                    }
                }
            } else {
                out.println("Unknown command");
                printSummary();
                System.exit(1);
            }
        } catch (InterruptedException ignored) {
            System.exit(130);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void generateToFile(String @NonNull [] names, Map<String, String> labels,
                                       Map<String, Integer> labelCount, long entityCount,
                                       File file, @NonNull String format) {
        try {
            if (!format.equals("json") && !format.equals("csv") && !format.equals("plain")) {
                for (String name : names) {
                    Set<MainWorker> workers = new HashSet<>();
                    for (long i = 0; i < entityCount; i++) {
                        MainWorker worker = new MainWorker(labels.get(name), labelCount.get(name), false);
                        workers.add(worker);
                        worker.start();
                    }

                    StringBuilder sb = new StringBuilder();
                    for (MainWorker worker : workers) {
                        worker.join();
                        String text = format.replace("{name}", name).replace("{value}", worker.getOutput());
                        sb.append(text).append("\n");
                    }
                    Files.writeString(Path.of(file.getAbsolutePath()), sb.toString(),
                            StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                }
            } else {
                switch (format) {
                    case "json" -> {
                        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                            List<Map<String, String>> allData = gen(names, labels, labelCount, entityCount);

                            writeJson(writer, allData);
                            out.println("Data successfully saved to " + file.getAbsolutePath());
                        } catch (IOException e) {
                            out.println("Error saving file: " + e.getMessage());
                        }
                    }
                    case "csv" -> {
                        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                            List<Map<String, String>> allData = gen(names, labels, labelCount, entityCount);

                            writeCsv(writer, names, allData);
                            out.println("Data successfully saved to " + file.getAbsolutePath());
                        } catch (IOException e) {
                            out.println("Error saving file: " + e.getMessage());
                            System.exit(1);
                        }
                    }
                    case "plain" -> {
                        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                            List<Map<String, String>> allData = gen(names, labels, labelCount, entityCount);

                            writePlain(writer, allData);
                            out.println("Data successfully saved to " + file.getAbsolutePath());
                        } catch (IOException e) {
                            out.println("Error saving file: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (InterruptedException ignored) {
            System.exit(130);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(130);
        }
    }

    private static @NonNull List<Map<String, String>> gen(String @NonNull [] names, Map<String, String> labels, Map<String, Integer> labelCount, long entityCount) {
        try {
            List<Map<String, String>> allData = new ArrayList<>();

            for (long row = 0; row < entityCount; row++) {
                Map<String, String> rowData = new LinkedHashMap<>();
                Set<MainWorker> workers = new HashSet<>();
                Map<String, MainWorker> workerMap = new HashMap<>();

                for (String name : names) {
                    MainWorker worker = new MainWorker(labels.get(name), labelCount.get(name), false);
                    workers.add(worker);
                    workerMap.put(name, worker);
                    worker.start();
                }

                for (MainWorker worker : workers) {
                    worker.join();
                    for (Map.Entry<String, MainWorker> entry : workerMap.entrySet()) {
                        if (entry.getValue() == worker) {
                            rowData.put(entry.getKey(), worker.getOutput());
                            break;
                        }
                    }
                }

                allData.add(rowData);
            }
            return allData;
        } catch (InterruptedException ignored) {
            System.exit(130);
        }
        throw new UnknownError();
    }

    private static void writeJson(@NonNull PrintWriter writer, @NonNull List<Map<String, String>> data) {
        writer.println("[");
        for (int i = 0; i < data.size(); i++) {
            Map<String, String> row = data.get(i);
            writer.print("  {");
            int j = 0;
            for (Map.Entry<String, String> entry : row.entrySet()) {
                writer.print("\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"");
                if (j < row.size() - 1) writer.print(", ");
                j++;
            }
            writer.print("}");
            if (i < data.size() - 1) writer.println(",");
            else writer.println();
        }
        writer.println("]");
    }

    private static void writeCsv(@NonNull PrintWriter writer, String[] names, @NonNull List<Map<String, String>> data) {
        writer.println(String.join(",", names));
        for (Map<String, String> row : data) {
            List<String> values = new ArrayList<>();
            for (String name : names) {
                String value = row.get(name);
                // Экранируем: если значение содержит запятую, кавычки или перенос строки — оборачиваем в кавычки
                if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
                    value = "\"" + value.replace("\"", "\"\"") + "\"";
                }
                values.add(value);
            }
            writer.println(String.join(",", values));
        }
    }

    private static void writePlain(PrintWriter writer, @NonNull List<Map<String, String>> data) {
        for (Map<String, String> row : data) {
            List<String> values = new ArrayList<>(row.values());
            writer.println(String.join(", ", values));
        }
    }

    private static void printSummary() {
        out.println("========================================================================");
        out.println("      DATA GENERATOR ENGINE v2.0 — REFERENCE GUIDE");
        out.println("========================================================================");
        out.println("This program is designed for high-performance generation of random");
        out.println("textual data (names, emails, IDs, hashes) based on a user-defined text template.\n");
        out.println("CONTROLS:");
        out.println("  1. Enter a list of names (columns/entities) separated by commas.");
        out.println("  2. For each name, specify a generation mask (format).");
        out.println("  3. Specify the average length (the generator will randomly vary it within ±3).");
        out.println("  4. Specify the number of rows to generate.\n");
        out.println("OUTPUT MODES:");
        out.println("  print  - Output generated data directly to the console.");
        out.println("  file   - Save generated data to a file with selectable formats.\n");
        out.println("FILE OUTPUT FORMATS:");
        out.println("  1 - JSON array of objects: [{\"name1\":\"value1\",\"name2\":\"value2\"},...]");
        out.println("      Each row contains all names as key-value pairs.");
        out.println("  2 - CSV with header: name1,name2,name3");
        out.println("      First line is the header, subsequent lines are values.");
        out.println("  3 - Plain text, one value per line (values separated by comma)");
        out.println("      Each row's values joined with \", \".");
        out.println("  4 - Custom format using template with {name} and {value} placeholders.");
        out.println("      Example: '{\"name\":\"{value}\"}' for each name-value pair.");
        out.println("      Note: Custom format generates entityCount values per name sequentially.\n");
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
        out.println("  For console output: data is generated per name sequentially.");
        out.println("  For file output:");
        out.println("    - JSON/CSV/Plain: data generated row-by-row, all names per row in parallel.");
        out.println("    - Custom format:   data generated per name, entityCount values in parallel.");
        out.println("========================================================================");
    }
}