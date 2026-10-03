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
					.because("controllers are singletons by default, so all request threads share their fields");

	private static final DescribedPredicate<JavaClass> KNOWN_NOT_THREAD_SAFE_JDK_TYPES =
			assignableTo(Format.class)
					.or(assignableTo(Calendar.class))
					.or(assignableTo(StringBuilder.class))
					.as("JDK types that are not thread-safe (Format, Calendar, StringBuilder)");

	@ArchTest
	static final ArchRule controllers_should_not_hold_known_not_thread_safe_jdk_types =
			fields().that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
					.should().notHaveRawType(KNOWN_NOT_THREAD_SAFE_JDK_TYPES)
					.because("JDK classes carry no thread safety annotations, so a list blocks them");
}
