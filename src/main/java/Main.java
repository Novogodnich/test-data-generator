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
        out.println("      DATA GENERATOR ENGINE v1.0 — СПРАВКА ПО ИСПОЛЬЗОВАНИЮ");
        out.println("========================================================================");
        out.println("Программа предназначена для высокопроизводительной генерации случайных");
        out.println("текстовых данных (имен, email, ID, хэшей) по заданному текстовому шаблону.");
        out.println();
        out.println("УПРАВЛЕНИЕ:");
        out.println("  1. Введите список имён (колонок/сущностей) через запятую.");
        out.println("  2. Для каждого имени задайте маску генерации (формат).");
        out.println("  3. Укажите среднюю длину (генератор будет случайно варьировать её в пределах +-3).");
        out.println("  4. Укажите количество строк для генерации.");
        out.println();
        out.println("ПРАВИЛА ЗАДАНИЯ МАСКИ:");
        out.println("  Ключевое слово 'GEN' является маркером вставки случайных символов.");
        out.println("  Всё, что написано вокруг 'GEN', остаётся неизменным (статический текст).");
        out.println();
        out.println("Примеры масок:");
        out.println("  - GEN@GEN.ru          ->  случайнаяСтрока@случайнаяСтрока.ru");
        out.println("  - id_GEN_2026         ->  id_случайнаяСтрока_2026");
        out.println("  - +7(999)GEN-GEN-GEN  ->  телефонный номер со случайными блоками букв");
        out.println();
        out.println("АРХИТЕКТУРА И МНОГОПОТОЧНОСТЬ:");
        out.println("  Генерация для каждой сущности происходит параллельно в изолированных");
        out.println("  потоках ядра ОС. Потоки синхронизированы через барьеры ожидания (Thread.join),");
        out.println("  что гарантирует упорядоченный вывод без состояния гонки (Race Condition).");
        out.println("========================================================================");
    }
}