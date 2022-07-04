package com.jeroensteenbeeke.vavr.hamcrest;

import io.vavr.concurrent.Future;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.hamcrest.Description;
import org.hamcrest.StringDescription;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.intellij.lang.annotations.Pattern;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.jeroensteenbeeke.vavr.hamcrest.VavrMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class VavrMatchersTest {
	@Test
	void constructorShouldNotBeInvokable() {
		assertThrows(UnsupportedOperationException.class, VavrMatchers::new);
	}

	@Test
	void testOptionMatchers() {
		assertThat(Option.none(), isNone());
		assertThat(Option.some("A"),
				fails(isNone()).withMismatchDescription("is an Option with a value equal to \"A\""));
		assertThat(Option.none(), isEmptyOption());
		assertThat(Option.some("A"),
				fails(isEmptyOption()).withMismatchDescription("is an Option with a value equal to \"A\""));

		assertThat(Option.some("A"), isDefinedOption());
		assertThat(Option.none(), fails(isDefinedOption()).withMismatchDescription("is an empty Option"));
		assertThat(Option.some("A"), isDefinedOption("A"));
		assertThat(Option.some("B"),
				fails(isDefinedOption("A")).withMismatchDescription("is an Option with a value equal to \"B\""));
		assertThat(Option.none(), fails(isDefinedOption("A")).withMismatchDescription("is an empty Option"));

		assertThat(Option.some("A"), isSome());
		assertThat(Option.none(), fails(isSome()).withMismatchDescription("is an empty Option"));
		assertThat(Option.some("A"), isSome("A"));
		assertThat(Option.some("B"),
				fails(isSome("A")).withMismatchDescription("is an Option with a value equal to \"B\""));
		assertThat(Option.none(), fails(isSome("A")).withMismatchDescription("is an empty Option"));

		assertThat(Option.some("A"),
				isDefinedOption("contains only alphabetic letters", s -> s.matches("^[a-zA-Z]*$")));
		assertThat(Option.some("1"), fails(VavrMatchers.<String>isDefinedOption("contains only alphabetic letters",
				s -> s.matches("^[a-zA-Z]*$"))).withMismatchDescription(
				"is an Option with a value not matching \"contains only alphabetic letters\", because the value is equal to \"1\""));
		assertThat(Option.none(), fails(VavrMatchers.<String>isDefinedOption("contains only alphabetic letters",
				s -> s.matches("^[a-zA-Z]*$"))).withMismatchDescription("is an empty Option"));
	}

	@Test
	void testEitherMatchers() {
		assertThat(Either.left("Error"), isLeft());
		assertThat(Either.right("Error"),
				fails(isLeft()).withMismatchDescription("is a right Either, with value \"Error\""));

		assertThat(Either.left("Error"), isLeft("Error"));
		assertThat(Either.left("ErRoR"),
				fails(isLeft("Error")).withMismatchDescription("is a left Either, with value \"ErRoR\""));
		assertThat(Either.right("Error"),
				fails(isLeft("Error")).withMismatchDescription("is a right Either, with value \"Error\""));
		assertThat(Either.left("Error"), isLeft("Starts with an E", s -> s.startsWith("E")));
		assertThat(Either.left("Arror"),
				fails(VavrMatchers.<String>isLeft("Starts with an E", s -> s.startsWith("E"))).withMismatchDescription(
						"is a left Either, with a value not matching \"Starts with an E\", because the value is equal to \"Arror\""));
		assertThat(Either.right("Error"),
				fails(VavrMatchers.<String>isLeft("Starts with an E", s -> s.startsWith("E"))).withMismatchDescription(
						"is a right Either, with value \"Error\""));

		assertThat(Either.right("Error"), isRight());
		assertThat(Either.left("Error"),
				fails(isRight()).withMismatchDescription("is a left Either, with value \"Error\""));

		assertThat(Either.right("Error"), isRight("Error"));
		assertThat(Either.right("ErRoR"),
				fails(isRight("Error")).withMismatchDescription("is a right Either, with value \"ErRoR\""));
		assertThat(Either.left("Error"),
				fails(isRight("Error")).withMismatchDescription("is a left Either, with value \"Error\""));
		assertThat(Either.right("Error"), isRight("Starts with an E", s -> s.startsWith("E")));
		assertThat(Either.right("Arror"),
				fails(VavrMatchers.<String>isRight("Starts with an E", s -> s.startsWith("E"))).withMismatchDescription(
						"is a right Either, with a value not matching \"Starts with an E\", because the value is equal to \"Arror\""));
		assertThat(Either.left("Error"),
				fails(VavrMatchers.<String>isRight("Starts with an E", s -> s.startsWith("E"))).withMismatchDescription(
						"is a left Either, with value \"Error\""));
	}

	@Test
	void testTryMatchers() {
		assertThat(Try.success("S"), isSuccess());
		assertThat(Try.failure(new RuntimeException()), fails(isSuccess()).withMismatchDescription(
				"is a failure, with exception of type <class java.lang.RuntimeException>"));

		assertThat(Try.success("S"), isSuccess("S"));
		assertThat(Try.success("B"), fails(isSuccess("S")).withMismatchDescription("is a success, with value \"B\""));
		assertThat(Try.failure(new RuntimeException()), fails(isSuccess("S")).withMismatchDescription(
				"is a failure, with exception of type <class java.lang.RuntimeException>"));

		assertThat(Try.success("S"), isSuccess("Starts with an S", v -> v.startsWith("S")));
		assertThat(Try.success("B"), fails(VavrMatchers.<String>isSuccess("Starts with an S",
				v -> v.startsWith("S"))).withMismatchDescription(
				"is a success, which does not match \"Starts with an S\", because the value is equal to \"B\""));
		assertThat(Try.failure(new RuntimeException()), fails(VavrMatchers.<String>isSuccess("Starts with an S",
				v -> v.startsWith("S"))).withMismatchDescription(
				"is a failure, with exception of type <class java.lang.RuntimeException>"));

		assertThat(Try.failure(new RuntimeException()), isFailure());
		assertThat(Try.success("S"), fails(isFailure()).withMismatchDescription("is a success, with value \"S\""));

		assertThat(Try.failure(new IllegalStateException()), isFailure(IllegalStateException.class));
		assertThat(Try.failure(new IllegalArgumentException()),
				fails(isFailure(IllegalStateException.class)).withMismatchDescription(
						"is a failure, with exception of type \"java.lang.IllegalArgumentException\""));
		assertThat(Try.success("S"), fails(isFailure(IllegalStateException.class)).withMismatchDescription(
				"is a success, with value \"S\""));

		assertThat(Try.failure(new IllegalArgumentException()),
				isFailure("Is a runtime exception", t -> t instanceof RuntimeException));
		assertThat(Try.failure(new IOException()),
				fails(isFailure("Is a runtime exception", t -> t instanceof RuntimeException)).withMismatchDescription(
						"is a failure, not matching \"Is a runtime exception\", and exception \"java.io.IOException\""));
		assertThat(Try.success("Great success!"),
				fails(isFailure("Is a runtime exception", t -> t instanceof RuntimeException)).withMismatchDescription(
						"is a success, with value \"Great success!\""));
	}

	@Test
	void testFutureMatchers() {
		assertThat(Future.of(() -> "A"), isFuture());
		assertThat(Future.of(() -> "A"), isFuture("A"));
		assertThat(Future.of(() -> "B"),
				fails(VavrMatchers.<String>isFuture("A")).withMismatchDescription(
						"is a Future, that succeeds, with value \"B\""));

		assertThat(Future.of(() -> "A"), isFuture("A"));
		assertThat(Future.of(() -> "A"),
				fails(VavrMatchers.<String>isFailedFuture(IllegalStateException.class)).withMismatchDescription(
						"is a Future, that succeeds, and yields value \"A\""));
		assertThat(Future.of(() -> "A"), isFuture("A").withTimeout(1, TimeUnit.SECONDS));
		assertThat(Future.of(() -> {
			Thread.sleep(1500);
			return "A";
		}), fails(isFuture("A").withTimeout(1, TimeUnit.SECONDS)).withMismatchDescription(
				"is a Future, that fails by exceeding timeout"));
		assertThat(Future.of(() -> {
			throw new IllegalStateException();
		}), isFailedFuture());
		assertThat(Future.of(() -> {
			throw new IllegalStateException();
		}), isFailedFuture(IllegalStateException.class));
		assertThat(Future.of(() -> {
			throw new IllegalStateException();
		}), fails(VavrMatchers.<String>isFailedFuture(IllegalArgumentException.class)).withMismatchDescription(
				"is a Future, that fails, with exception of type \"java.lang.IllegalStateException\""));
		assertThat(Future.of(() -> {
			throw new IllegalStateException("Illegal State");
		}), isFailedFuture(IllegalStateException.class));
		assertThat(Future.of(() -> {
			throw new IllegalStateException("Illegal State");
		}), isFailedFuture(IllegalStateException.class).withMessage("Illegal State"));
		assertThat(Future.of(() -> {
			throw new IllegalStateException("Illegal State");
		}), fails(VavrMatchers.<String>isFailedFuture(IllegalArgumentException.class)).withMismatchDescription(
				"is a Future, that fails, with exception of type \"java.lang.IllegalStateException\", with message \"Illegal State\""));

		assertThat(Future.of(() -> {
			throw new IllegalStateException("IlLeGaL sTaTe");
		}), fails(VavrMatchers.<String>isFailedFuture(IllegalArgumentException.class)
				.withMessage("Illegal State")).withMismatchDescription(
				"is a Future, that fails, with exception of type \"java.lang.IllegalStateException\", with message \"IlLeGaL sTaTe\""));

		assertThat(Future.of(() -> 5), fails(isFuture().withTimeout(-1, TimeUnit.SECONDS)).withMismatchDescription(
				"invalid parameter timeoutAmount, must be positive, but is <-1L>"));
		assertThat(Future.of(() -> 5), fails(isFuture().withTimeout(0, TimeUnit.SECONDS)).withMismatchDescription(
				"invalid parameter timeoutAmount, must be positive, but is <0L>"));
		assertThat(Future.of(() -> 5), isFuture().withTimeout(1, TimeUnit.SECONDS));

		assertThat(Future.of(() -> 5), fails(VavrMatchers.<Integer>isFailedFuture()).withMismatchDescription(
				"is a Future, that succeeds, and yields value <5>"));
		assertThat(Future.of(() -> 5),
				fails(VavrMatchers.<Integer>isFailedFuture().withTimeout(1L, TimeUnit.SECONDS)).withMismatchDescription(
						"is a Future, that succeeds, and yields value <5>"));
		assertThat(Future.of(() -> 5), fails(isFailedFuture(IllegalStateException.class).withTimeout(1,
				TimeUnit.SECONDS)).withMismatchDescription(
				"is a Future, that succeeds, and yields value <5>"));
		assertThat(Future.of(() -> 5), fails(isFailedFuture(IllegalStateException.class).withMessage("This is an error")
				.withTimeout(1, TimeUnit.SECONDS)).withMismatchDescription(
				"is a Future, that succeeds, and yields value <5>"));
		assertThat(Future.of(() -> {
			throw new IllegalStateException("This is an error");
		}), isFailedFuture(IllegalStateException.class).withMessage("This is an error")
				.withTimeout(1, TimeUnit.SECONDS));
		assertThat(Future.of(() -> {
			throw new IllegalStateException("This is an error");
		}), fails(isFailedFuture(IllegalStateException.class).withMessage("This is not an error")
				.withTimeout(1, TimeUnit.SECONDS)).withMismatchDescription(
				"is a Future, that fails, with exception of type \"java.lang.IllegalStateException\", with message \"This is an error\""));
		assertThat(Future.of(() -> {
			throw new IllegalStateException("Illegal State");
		}), isFailedFuture(IllegalStateException.class).withMessage("Illegal State").withTimeout(1, TimeUnit.SECONDS));
		assertThat(Future.of(() -> {
			throw new IllegalStateException("Illegal State");
		}), fails(VavrMatchers.<String>isFuture()).withMismatchDescription(
				"is a Future, that fails, with exception of type \"java.lang.IllegalStateException\", with message \"Illegal State\""));
		assertThat(Future.of(() -> {
			throw new IllegalStateException("Illegal State");
		}), fails(VavrMatchers.<String>isFuture("5")).withMismatchDescription(
				"is a Future, that fails, with exception of type \"java.lang.IllegalStateException\", with message \"Illegal State\""));

		assertThat(new HackedFuture(),
				fails(VavrMatchers.<String>isFailedFuture(IllegalStateException.class)).withMismatchDescription(
						"is a Future, that fails, but has no defined failure cause"));

		assertThat(new HackedFuture(),
				VavrMatchers.<String>isFailedFuture());

		assertThat(Future.of(() -> 5),
				VavrMatchers.<Integer>isFutureMatching("Value greater than or equal to 5", v -> v >= 5)
						.withTimeout(1L, TimeUnit.SECONDS));
		assertThat(Future.of(() -> 5),
				VavrMatchers.<Integer>isFutureMatching("Value greater than or equal to 5", v -> v >= 5));
		assertThat(Future.of(() -> 4), fails(VavrMatchers.<Integer>isFutureMatching("Value greater than or equal to 5",
				v -> v >= 5)).withMismatchDescription(
				"is a Future, that succeeds, with value <4> not satisfying \"Value greater than or equal to 5\""));
		assertThat(Future.of(() -> {
			throw new IllegalStateException("Illegal State");
		}), fails(VavrMatchers.<Integer>isFutureMatching("Value greater than or equal to 5",
				v -> v >= 5)).withMismatchDescription(
				"is a Future, that fails, with exception of type \"java.lang.IllegalStateException\", with message \"Illegal State\""));
		assertThat(Future.of(() -> {
			throw new IllegalStateException("Illegal State");
		}), VavrMatchers.<Integer>isFailedFutureMatching("Exception has message 'Illegal State'",
				t -> "Illegal State".equals(t.getMessage())));
		assertThat(Future.of(() -> {
			throw new IllegalStateException("Illegal State");
		}), VavrMatchers.<Integer>isFailedFutureMatching("Exception has message 'Illegal State'",
				t -> "Illegal State".equals(t.getMessage())));
		assertThat(Future.of(() -> {
			throw new IllegalStateException("Illegal State");
		}), fails(VavrMatchers.<Integer>isFailedFutureMatching("Exception does not have message 'Illegal State'",
				t -> !"Illegal State".equals(t.getMessage()))).withMismatchDescription(
				"is a Future, that fails, with exception not matching predicate \"Exception does not have message 'Illegal State'\""));

		assertThat(Future.of(() -> 5),
				fails(VavrMatchers.<Integer>isFailedFutureMatching("Exception does not have message 'Illegal State'",
								t -> !"Illegal State".equals(t.getMessage()))
						.withTimeout(5, TimeUnit.SECONDS)).withMismatchDescription(
						"is a Future, that succeeds, and yields value <5>"));

		assertThat(new HackedFuture(),
				fails(VavrMatchers.<String>isFailedFutureMatching("Exception does not have message 'Illegal State'",
								t -> !"Illegal State".equals(t.getMessage()))
						.withTimeout(5, TimeUnit.SECONDS)).withMismatchDescription(
						"is a Future, that fails, but has no defined failure cause"));
	}


	@Test
	void testDescriptions() {
		assertThat(descriptionOf(isNone()), equalTo("is an empty Option"));
		assertThat(descriptionOf(isEmptyOption()), equalTo("is an empty Option"));
		assertThat(descriptionOf(isSome()), equalTo("is an Option with a value"));
		assertThat(descriptionOf(isDefinedOption()), equalTo("is an Option with a value"));
		assertThat(descriptionOf(isSome("A")), equalTo("is an Option with a value equal to \"A\""));
		assertThat(descriptionOf(isDefinedOption("A")), equalTo("is an Option with a value equal to \"A\""));
		assertThat(descriptionOf(isDefinedOption("A", v -> true)), equalTo("is an Option with a value matching \"A\""));

		assertThat(descriptionOf(isLeft()), equalTo("is a left Either"));
		assertThat(descriptionOf(isLeft("L")), equalTo("is a left Either, with value \"L\""));
		assertThat(descriptionOf(isLeft("L", v -> true)), equalTo("is a left Either, with a value matching \"L\""));
		assertThat(descriptionOf(isRight()), equalTo("is a right Either"));
		assertThat(descriptionOf(isRight("R")), equalTo("is a right Either, with value \"R\""));
		assertThat(descriptionOf(isRight("Predicate", v -> true)),
				equalTo("is a right Either, with a value matching \"Predicate\""));

		assertThat(descriptionOf(isSuccess()), equalTo("is a success"));
		assertThat(descriptionOf(isSuccess("S")), equalTo("is a success, with value \"S\""));
		assertThat(descriptionOf(VavrMatchers.<String>isSuccess("Starts with an S", v -> v.startsWith("S"))),
				equalTo("is a success, matching \"Starts with an S\""));

		assertThat(descriptionOf(isFailure()), equalTo("is a failure"));
		assertThat(descriptionOf(isFailure(IllegalStateException.class)),
				equalTo("is a failure, with exception of type \"java.lang.IllegalStateException\""));
		assertThat(descriptionOf(isFailure("Is a runtime exception", t -> t instanceof RuntimeException)),
				equalTo("is a failure, with throwable matching \"Is a runtime exception\""));

		assertThat(descriptionOf(isFuture()), equalTo("is a Future, that succeeds"));
		assertThat(descriptionOf(isFuture().withTimeout(5, TimeUnit.SECONDS)),
				equalTo("is a Future, that completes within 5 seconds, that succeeds"));
		assertThat(descriptionOf(VavrMatchers.<String>isFuture("F")),
				equalTo("is a Future, that succeeds, with value \"F\""));
		assertThat(descriptionOf(isFuture("F")), equalTo("is a Future, that succeeds, with value \"F\""));
		assertThat(descriptionOf(isFuture("F").withTimeout(5, TimeUnit.SECONDS)),
				equalTo("is a Future, that completes within 5 seconds, that succeeds, with value \"F\""));
		assertThat(descriptionOf(isFailedFuture()),
				equalTo("is a Future, that fails"));
		assertThat(descriptionOf(isFailedFuture(IllegalStateException.class)),
				equalTo("is a Future, that fails, with exception of type \"java.lang.IllegalStateException\""));
		assertThat(descriptionOf(isFailedFuture(IllegalStateException.class).withMessage("Illegal State")),
				equalTo("is a Future, that fails, with exception of type \"java.lang.IllegalStateException\" and message \"Illegal State\""));
		assertThat(descriptionOf(VavrMatchers.<Integer>isFutureMatching("greater than 5", v -> v > 5)),
				equalTo("is a Future, that succeeds, with value matching predicate \"greater than 5\""));

		assertThat(descriptionOf(VavrMatchers.<Integer>isFailedFutureMatching("IllegalStateException",
						ex -> ex instanceof IllegalStateException)),
				equalTo("is a Future, that fails, with exception matching predicate \"IllegalStateException\""));

		assertThat(descriptionOf(isFuture().withTimeout(-1, TimeUnit.SECONDS)), equalTo("is a Future, that succeeds"));
		assertThat(descriptionOf(isFuture().withTimeout(0, TimeUnit.SECONDS)), equalTo("is a Future, that succeeds"));
		assertThat(descriptionOf(isFuture().withTimeout(1, TimeUnit.SECONDS)),
				equalTo("is a Future, that completes within 1 seconds, that succeeds"));
	}

	private static <T> WithMismatchDescription<T> fails(TypeSafeDiagnosingMatcher<T> matcher) {
		return expectedMismatchDescription -> new TypeSafeDiagnosingMatcher<T>() {
			@Override
			protected boolean matchesSafely(T t, Description description) {
				if (matcher.matches(t)) {
					description.appendText("succeeds");
				} else {
					description.appendText("fails, with description ");

					StringDescription delegate = new StringDescription();
					matcher.describeMismatch(t, delegate);

					String actualMismatchDescription = delegate.toString();
					if (actualMismatchDescription.equals(expectedMismatchDescription)) {
						return true;
					}
					description.appendValue(actualMismatchDescription);
				}

				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("fails, with description ");
				description.appendValue(expectedMismatchDescription);

			}
		};
	}


	@FunctionalInterface
	private interface WithMismatchDescription<T> {
		@NotNull
		TypeSafeDiagnosingMatcher<T> withMismatchDescription(
				@NotNull @Pattern("^.+$") String expectedMismatchDescription);
	}
}
