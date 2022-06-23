package com.jeroensteenbeeke.vavr.hamcrest;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaParameter;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

public class MethodSignatureTest {
	@Test
	void testAnnotationPresence() {
		ArchRule allPublicStaticMethodsShouldNotReturnNull = methods().that().areStatic().and().arePublic().should()
				.beAnnotatedWith(NotNull.class).andShould(haveNoNullableParameters())
				.because("No method should return null, and have no argument that accepts null");

		JavaClasses classes = new ClassFileImporter().importPackages(VavrMatchers.class.getPackage().getName());

		allPublicStaticMethodsShouldNotReturnNull.check(classes);

	}

	private ArchCondition<? super JavaMethod> haveNoNullableParameters() {
		return new ArchCondition<JavaMethod>("method should have no parameters that accept null") {
			@Override
			public void check(JavaMethod method, ConditionEvents events) {
				for (JavaParameter param : method.getParameters()) {
					if (param.getType().toErasure().isPrimitive()) {
						// Primitives never accept null, annotation not necessary
						continue;
					}

					if (param.isAnnotatedWith(NotNull.class)) {
						continue;
					}

					events.add(new SimpleConditionEvent(method, false,
							String.format("Parameter %d of method %s is not annotated @NotNull", param.getIndex(),
									method.getFullName())));
					return;
				}

				events.add(new SimpleConditionEvent(method, true,
						String.format("All parameters of %s are annotated @NotNull", method.getFullName())));
			}
		};
	}
}
