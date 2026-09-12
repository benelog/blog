import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;

/**
 * 읽지 않을 출력을 리다이렉트하는 네 가지 방법.
 * 모두 588,895바이트를 출력하지만 Java 코드가 비워야 할 파이프가 없어서 멈추지 않는다.
 * 앞의 두 방식은 표준 출력으로 100,000줄씩 내보내므로 실행 결과를 파일로 받는 편이 좋다.
 */
public class RedirectDemo {
    public static void main(String[] args) throws IOException, InterruptedException {
        // 부모의 표준 출력과 표준 오류로 바로 내보내기
        new ProcessBuilder("seq", "1", "100000")
                .redirectOutput(Redirect.INHERIT)
                .redirectError(Redirect.INHERIT)
                .start()
                .waitFor();

        // 표준 입력까지 세 스트림을 한 번에 물려주기
        new ProcessBuilder("seq", "1", "100000")
                .inheritIO()
                .start()
                .waitFor();

        // 출력을 /dev/null로 버리기
        new ProcessBuilder("seq", "1", "100000")
                .redirectOutput(Redirect.DISCARD)
                .redirectError(Redirect.DISCARD)
                .start()
                .waitFor();

        // 파일로 보내기
        new ProcessBuilder("seq", "1", "100000")
                .redirectOutput(Redirect.to(new File("seq-out.log")))
                .redirectError(Redirect.to(new File("seq-err.log")))
                .start()
                .waitFor();
    }
}
