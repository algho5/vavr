package com.jeroensteenbeeke.vavr.hamcrest;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaParameter;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import io.vavr.collection.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;


import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

public class MethodSignatureTest {
	@Test
	void testStaticMethodAnnotationPresence() {
		ArchRule allPublicStaticMethodsShouldNotReturnNull = methods().that().areStatic().and().arePublic().should()
				.beAnnotatedWith(NotNull.class).andShould(haveNoNullableParameters())
				.because("No method should return null, and have no argument that accepts null");

		JavaClasses classes = new ClassFileImporter().importPackages(VavrMatchers.class.getPackage().getName());

		allPublicStaticMethodsShouldNotReturnNull.check(classes);

	}

	@Test
	void testFutureMatcherAnnotationPresence() {
		JavaClasses classes = new ClassFileImporter().importPackages(VavrMatchers.class.getPackage().getName());

		List<Class<?>> classesToTest = List.of(
				FutureMatcher.class,
				FutureMatcher.FailureOfType.class,
				FutureMatcher.Failure.class,
				FutureMatcher.Success.class,
				FutureMatcher.SuccessMatchingPredicate.class,
				LazyMatcher.class,
				LazyMatcher.Bare.class,
				LazyMatcher.Valued.class
		);

		for (Class<?> classToTest : classesToTest) {
			ArchRule allPublicMethodsShouldNotReturnNull = methods().that().areDeclaredIn(classToTest).and()
					.arePublic().and().doNotHaveRawReturnType("void").and().doNotHaveRawReturnType("boolean").should()
					.beAnnotatedWith(NotNull.class).andShould(haveAnnotatedParameters())
					.because("No method should return null, and must have all reference parameters properly annotated NotNull or Nullable").allowEmptyShould(true);
			ArchRule allProtectedMethodsShouldNotReturnNull = methods().that().areDeclaredIn(classToTest).and()
					.areProtected().and().doNotHaveRawReturnType("void").and().doNotHaveRawReturnType("boolean").should()
					.beAnnotatedWith(NotNull.class).andShould(haveAnnotatedParameters())
					.because("No method should return null, and must have all object parameters properly annotated NotNull or Nullable").allowEmptyShould(true);
			ArchRule allPublicVoidMethodsShouldHaveParamAnnotations = methods().that().areDeclaredIn(classToTest).and()
					.arePublic().and().haveRawReturnType("void").should(haveAnnotatedParameters())
					.because("All public methods must have all reference parameters properly annotated NotNull or Nullable").allowEmptyShould(true);
			ArchRule allPublicBooleanMethodsShouldHaveParamAnnotations = methods().that().areDeclaredIn(classToTest).and()
					.arePublic().and().haveRawReturnType("boolean").should(haveAnnotatedParameters())
					.because("All public methods and must have all reference parameters properly annotated NotNull or Nullable").allowEmptyShould(true);
			ArchRule allProtecedVoidMethodsShouldHaveParamAnnotations = methods().that().areDeclaredIn(classToTest).and()
					.areProtected().and().haveRawReturnType("void").should(haveAnnotatedParameters())
					.because("All protected methods must have all reference parameters properly annotated NotNull or Nullable").allowEmptyShould(true);
			ArchRule allProtecedBooleanMethodsShouldHaveParamAnnotations = methods().that().areDeclaredIn(classToTest).and()
					.areProtected().and().haveRawReturnType("boolean").should(haveAnnotatedParameters())
					.because("All protected methods and must have all reference parameters properly annotated NotNull or Nullable").allowEmptyShould(true);


			allPublicMethodsShouldNotReturnNull.check(classes);
			allProtectedMethodsShouldNotReturnNull.check(classes);
			allPublicVoidMethodsShouldHaveParamAnnotations.check(classes);
			allPublicBooleanMethodsShouldHaveParamAnnotations.check(classes);
			allProtecedVoidMethodsShouldHaveParamAnnotations.check(classes);
			allProtecedBooleanMethodsShouldHaveParamAnnotations.check(classes);
		}



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

	private ArchCondition<? super JavaMethod> haveAnnotatedParameters() {
		return new ArchCondition<JavaMethod>("method should have no parameters that accept null") {
			@Override
			public void check(JavaMethod method, ConditionEvents events) {
				for (JavaParameter param : method.getParameters()) {
					if (param.getType().toErasure().isPrimitive()) {
						// Primitives never accept null, annotation not necessary
						continue;
					}

					if (param.isAnnotatedWith(NotNull.class) || param.isAnnotatedWith(Nullable.class)) {
						continue;
					}

					events.add(new SimpleConditionEvent(method, false,
							String.format("Parameter %d of method %s is not annotated @NotNull or @Nullable", param.getIndex(),
									method.getFullName())));
					return;
				}

				events.add(new SimpleConditionEvent(method, true,
						String.format("All parameters of %s are annotated @NotNull or @Nullable", method.getFullName())));
			}
		};
	}
}
