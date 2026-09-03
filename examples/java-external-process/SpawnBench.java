import java.io.IOException;

public class SpawnBench {
    public static void main(String[] args) throws IOException, InterruptedException {
        int count = Integer.parseInt(args[0]);
        String mode = System.getProperty("jdk.lang.Process.launchMechanism", "default");
        String[] command = {"true"};
        // warm-up
        for (int i = 0; i < 5; i++) new ProcessBuilder(command).start().waitFor();
        long start = System.nanoTime();
        for (int i = 0; i < count; i++) {
            new ProcessBuilder(command).start().waitFor();
        }
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("%s: %d spawns, %d ms, %.2f ms/spawn%n", mode, count, elapsedMs, (double) elapsedMs / count);
    }
}
