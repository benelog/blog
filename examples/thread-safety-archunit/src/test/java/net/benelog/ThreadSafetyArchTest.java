package net.benelog;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;

import java.text.Format;
import java.util.Calendar;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import net.jcip.annotations.NotThreadSafe;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(packages = "net.benelog")
class ThreadSafetyArchTest {

	@ArchTest
	static final ArchRule controllers_should_not_hold_not_thread_safe_types =
			fields().that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
					.should().notHaveRawType(annotatedWith(NotThreadSafe.class))
					.because("controller는 singleton이라 모든 요청 스레드가 필드를 공유한다");

	private static final DescribedPredicate<JavaClass> KNOWN_NOT_THREAD_SAFE_JDK_TYPES =
			assignableTo(Format.class)
					.or(assignableTo(Calendar.class))
					.or(assignableTo(StringBuilder.class))
					.as("JDK의 스레드 안전하지 않은 타입(Format, Calendar, StringBuilder)");

	@ArchTest
	static final ArchRule controllers_should_not_hold_known_not_thread_safe_jdk_types =
			fields().that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
					.should().notHaveRawType(KNOWN_NOT_THREAD_SAFE_JDK_TYPES)
					.because("JDK 클래스에는 스레드 안전성 애너테이션이 없으므로 목록으로 막는다");
}
