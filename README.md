# Multithreaded Data Generator Engine (Core Java)

A high-performance console engine built on pure Java SE for generating realistic text data based on user-defined masks and templates. The project is developed without heavyweight frameworks (Spring/Hibernate), focusing on native multithreading and efficient memory management.

## 🔥 Key Features and Architecture

*   **Pure Java Core:** No Spring overhead — only efficient data structures and built-in concurrency mechanisms.
*   **Safe Multithreading:** Data generation for each entity is isolated in separate OS kernel threads. Output synchronization is implemented via `Thread.join()` wait barriers, completely eliminating race conditions and console output interleaving.
*   **Smart Mask Composition:** Template parsing (e.g., `GEN@GEN.ru` or `id-GEN-2026`) is implemented through linear "sandwich" assembly based on `StringBuilder`, guaranteeing $O(N)$ performance and eliminating `IndexOutOfBoundsException` errors.
*   **Random Optimizations:** Use of a thread-safe generator to prevent locks between competing threads.
*   **Test Coverage:** Worker logic is fully covered by unit tests, verifying different execution modes (asynchronous `start()` and synchronous `run()`).

## 🛠 Technology Stack

*   **Language:** Java 21+ (or any modern LTS version)
*   **Concurrency API:** Threading, Join Barrier Synchronization
*   **Testing:** TestNG / JUnit
*   **Boilerplate Reduction:** Project Lombok

## 🚀 Quick Start and Usage Example

When launched, the application provides an interactive CLI interface for configuring generation parameters:

```text
Enter start for start
> start
Enter names of labels in format: "name, name2".
> email, test_id
Enter regex for name email in format: "GEN@GEN.ru".
> GEN@GEN.ru
Enter regex for name test_id in format: "GEN@GEN.ru".
> ID_GEN_2026
Enter average length for name email:
> 5
Enter average length for name test_id:
> 8
Enter count of entities for each name.
> 3
Starting!
Engine output result:

Values for name email:
abcde@fghij.ru
xyz@klmn.ru
qwert@yuiop.ru

Values for name test_id:
ID_asdfghjk_2026
ID_qwerty_2026
ID_zxcvbnma_2026