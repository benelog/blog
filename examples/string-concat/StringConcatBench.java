/**
 * 반복문 안에서 문자열을 누적할 때 `+` 결합과 StringBuilder 직접 사용의 성능 차이를 재는 단순 측정 코드.
 * JMH가 아닌 System.nanoTime() 기반이므로 미세한 수치보다는 배율의 규모를 보는 용도입니다.
 *
 * 실행: java StringConcatBench.java
 */
public class StringConcatBench {

    static String plusConcat(int n) {
        String s = "";
        for (int i = 0; i < n; i++) {
            s = s + "1111";
        }
        return s;
    }

    static String sbAppend(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append("1111");
        }
        return sb.toString();
    }

    static long sink = 0;

    static double measureBestMs(java.util.function.IntFunction<String> f, int n, int rounds) {
        long best = Long.MAX_VALUE;
        for (int r = 0; r < rounds; r++) {
            long t0 = System.nanoTime();
            String s = f.apply(n);
            long t1 = System.nanoTime();
            sink += s.length();
            best = Math.min(best, t1 - t0);
        }
        return best / 1_000_000.0;
    }

    public static void main(String[] args) {
        // JIT 워밍업
        for (int i = 0; i < 200; i++) {
            sink += plusConcat(500).length();
            sink += sbAppend(500).length();
        }

        int[] sizes = {100, 1_000, 10_000, 100_000};
        System.out.printf("%-10s %15s %18s %8s%n", "반복 횟수", "+ 결합 (ms)", "StringBuilder (ms)", "배율");
        for (int n : sizes) {
            int rounds = n >= 100_000 ? 5 : 20;
            double plus = measureBestMs(StringConcatBench::plusConcat, n, rounds);
            double sb = measureBestMs(StringConcatBench::sbAppend, n, rounds);
            System.out.printf("%-10d %15.3f %18.3f %7.0fx%n", n, plus, sb, plus / sb);
        }
        System.out.println("(각 조건에서 여러 번 실행한 최소 시간, sink=" + sink + ")");
    }
}
