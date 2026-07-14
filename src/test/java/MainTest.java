import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

public class MainTest {
    @Test
    public void mainTest() {
        try {
            MainWorker worker = new MainWorker(".GEN.", 4, false);

            worker.start();
            worker.join();

            String output = worker.getOutput();

            assertTrue(output.length() < 10 && output.length() > 3 && output.startsWith(".") && output.endsWith(".") && !output.contains("GEN"));
        } catch (Exception ignored) {
            fail();
        }
    }

    @Test
    public void testNoStart() {
        try {
            MainWorker worker = new MainWorker(".GEN.", 4, false);

            worker.run();

            String output = worker.getOutput();

            assertTrue(output.length() < 10 && output.length() > 3 && output.startsWith(".") && output.endsWith(".") && !output.contains("GEN"));
        } catch (Exception ignored) {
            fail();
        }
    }
}