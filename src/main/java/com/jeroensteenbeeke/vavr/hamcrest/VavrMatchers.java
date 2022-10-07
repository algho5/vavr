package com.jeroensteenbeeke.vavr.hamcrest;

import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Defines Hamcrest matchers for Vavr's Option, Either and Try
 */
public final class VavrMatchers {
	VavrMatchers() {
		throw new UnsupportedOperationException();
	}

	// region Matchers for Option<T>

	/**
	 * Matches an Option containing any value
	 *
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static TypeSafeDiagnosingMatcher<Option<?>> isSome() {
		return isDefinedOption();
	}

	/**
	 * Matches an Option containing any value
	 *
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static TypeSafeDiagnosingMatcher<Option<?>> isDefinedOption() {
		return new TypeSafeDiagnosingMatcher<Option<?>>() {
			@Override
			protected boolean matchesSafely(Option<?> subject, Description mismatchDescription) {
				if (subject.isDefined()) {
					return true;
				} else {
					mismatchDescription.appendText("is an empty Option");
				}

				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is an Option with a value");
			}
		};
	}

	/**
	 * Matches an Option containing no value
	 *
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static TypeSafeDiagnosingMatcher<Option<?>> isNone() {
		return isEmptyOption();
	}

	/**
	 * Matches an Option containing no value
	 *
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static TypeSafeDiagnosingMatcher<Option<?>> isEmptyOption() {
		return new TypeSafeDiagnosingMatcher<Option<?>>() {
			@Override
			protected boolean matchesSafely(Option<?> subject, Description mismatchDescription) {
				if (subject.isEmpty()) {
					return true;
				} else {
					mismatchDescription.appendText("is an Option with a value equal to ").appendValue(subject.get());
				}

				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is an empty Option");
			}
		};
	}

	/**
	 * Matches an Option containing the given value
	 *
	 * @param value The value that should be contained in the Option
	 * @param <T>   The type of value that should be in the Option
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static <T> TypeSafeDiagnosingMatcher<Option<T>> isSome(@NotNull T value) {
		return isDefinedOption(value);
	}

	/**
	 * Matches an Option whose contents satisfy the given matcher
	 *
	 * @param matcher A Hamcrest matcher that should be satisfied
	 * @param <T>     The type of value that should be in the Option
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static <T> TypeSafeDiagnosingMatcher<Option<T>> isSome(@NotNull Matcher<T> matcher) {
		return isDefinedOption(matcher);
	}

	/**
	 * Matches an Option containing the given value
	 *
	 * @param value The value that should be contained in the Option
	 * @param <T>   The type of value that should be in the Option
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static <T> TypeSafeDiagnosingMatcher<Option<T>> isDefinedOption(@NotNull T value) {
		return new TypeSafeDiagnosingMatcher<Option<T>>() {
			@Override
			protected boolean matchesSafely(Option<T> subject, Description mismatchDescription) {
				if (subject.isDefined()) {
					T actualValue = subject.get();
					if (value.equals(actualValue)) {
						return true;
					} else {
						mismatchDescription.appendText("is an Option with a value equal to ").appendValue(actualValue);
					}
				} else {
					mismatchDescription.appendText("is an empty Option");
				}

				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is an Option with a value equal to ").appendValue(value);
			}
		};
	}

	/**
	 * Matches an Option whose contents satisfy the given matcher
	 *
	 * @param matcher A Hamcrest matcher that should be satisfied
	 * @param <T>     The type of value that should be in the Option
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static <T> TypeSafeDiagnosingMatcher<Option<T>> isDefinedOption(@NotNull Matcher<T> matcher) {
		return new TypeSafeDiagnosingMatcher<Option<T>>() {
			@Override
			protected boolean matchesSafely(Option<T> subject, Description mismatchDescription) {
				if (subject.isDefined()) {
					T actualValue = subject.get();
					if (matcher.matches(actualValue)) {
						return true;
					} else {
						mismatchDescription.appendText("is an Option with value ").appendValue(actualValue)
								.appendText(" not matching because ");
						matcher.describeMismatch(actualValue, mismatchDescription);
					}
				} else {
					mismatchDescription.appendText("is an empty Option");
				}

				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is an Option with a value matching ");
				matcher.describeTo(description);
			}
		};
	}

	/**
	 * Matches an Option with a value matching the given Predicate
	 *
	 * @param predicateDescription Describes the predicate for user feedback
	 * @param predicate            The predicate to match
	 * @param <T>                  The type of value that should be in the Option
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static <T> TypeSafeDiagnosingMatcher<Option<T>> isDefinedOption(
			@NotNull String predicateDescription, @NotNull Predicate<T> predicate) {
		return new TypeSafeDiagnosingMatcher<Option<T>>() {
			@Override
			protected boolean matchesSafely(Option<T> subject, Description mismatchDescription) {
				if (subject.isDefined()) {
					if (predicate.test(subject.get())) {
						return true;
					} else {
						mismatchDescription.appendText("is an Option with a value not matching ")
								.appendValue(predicateDescription)
								.appendText(", because the value is equal to ").appendValue(subject.get());
					}
				} else {
					mismatchDescription.appendText("is an empty Option");
				}

				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is an Option with a value matching ").appendValue(predicateDescription);
			}
		};
	}
	// endregion

	// region Matchers for Either<L,R>

	/**
	 * Matches an Either that is a Left (error)
	 *
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static TypeSafeDiagnosingMatcher<Either<?, ?>> isLeft() {
		return new TypeSafeDiagnosingMatcher<Either<?, ?>>() {
			@Override
			protected boolean matchesSafely(Either<?, ?> subject, Description mismatchDescription) {
				if (subject.isLeft()) {
					return true;
				}

				mismatchDescription.appendText("is a right Either, with value ").appendValue(subject.get());

				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is a left Either");
			}
		};
	}

	/**
	 * Matches an Either that is a Left (error), with the given value
	 *
	 * @param expectedValue The expected value contained in the left
	 * @param <T>           The type contained in the Either
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static <T> TypeSafeDiagnosingMatcher<Either<T, ?>> isLeft(@NotNull T expectedValue) {
		return new TypeSafeDiagnosingMatcher<Either<T, ?>>() {
			@Override
			protected boolean matchesSafely(Either<T, ?> subject, Description mismatchDescription) {
				if (subject.isLeft()) {
					T actualValue = subject.getLeft();

					if (actualValue.equals(expectedValue)) {
						return true;
					} else {
						mismatchDescription.appendText("is a left Either, with value ").appendValue(actualValue);
					}
				} else {
					mismatchDescription.appendText("is a right Either, with value ").appendValue(subject.get());
				}

				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is a left Either, with value ").appendValue(expectedValue);
			}
		};
	}

	/**
	 * Matches an Either that is a Left (error), with the given matcher
	 *
	 * @param matcher A matcher that the contents of the left should adhere to
	 * @param <T>     The type contained in the Either
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static <T> TypeSafeDiagnosingMatcher<Either<T, ?>> isLeft(@NotNull Matcher<T> matcher) {
		return new TypeSafeDiagnosingMatcher<Either<T, ?>>() {
			@Override
			protected boolean matchesSafely(Either<T, ?> subject, Description mismatchDescription) {
				if (subject.isLeft()) {
					T actualValue = subject.getLeft();

					if (matcher.matches(actualValue)) {
						return true;
					} else {
						mismatchDescription.appendText("is a left Either, with value ").appendValue(actualValue)
								.appendText(" not matching because ");
						matcher.describeMismatch(actualValue, mismatchDescription);
					}
				} else {
					mismatchDescription.appendText("is a right Either, with value ").appendValue(subject.get());
				}

				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is a left Either, matching ");
				matcher.describeTo(description);
			}
		};
	}

	/**
	 * Matches a left Either with a value matching the given Predicate
	 *
	 * @param predicateDescription Describes the predicate for user feedback
	 * @param predicate            The predicate to match
	 * @param <T>                  The type of value that should be in the Either
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static <T> TypeSafeDiagnosingMatcher<Either<T, ?>> isLeft(
			@NotNull String predicateDescription, @NotNull Predicate<T> predicate) {
		return new TypeSafeDiagnosingMatcher<Either<T, ?>>() {
			@Override
			protected boolean matchesSafely(Either<T, ?> subject, Description mismatchDescription) {
				if (subject.isLeft()) {
					T actualValue = subject.getLeft();

					if (predicate.test(actualValue)) {
						return true;
					} else {
						mismatchDescription.appendText("is a left Either, with a value not matching ")
								.appendValue(predicateDescription).appendText(", because the value is equal to ")
								.appendValue(actualValue);
					}
				} else {
					mismatchDescription.appendText("is a right Either, with value ").appendValue(subject.get());
				}

				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is a left Either, with a value matching ").appendValue(predicateDescription);
			}
		};
	}

	/**
	 * Matches an Either that is a Right (success)
	 *
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static TypeSafeDiagnosingMatcher<Either<?, ?>> isRight() {
		return new TypeSafeDiagnosingMatcher<Either<?, ?>>() {
			@Override
			protected boolean matchesSafely(Either<?, ?> subject, Description mismatchDescription) {
				if (subject.isRight()) {
					return true;
				}

				mismatchDescription.appendText("is a left Either, with value ").appendValue(subject.getLeft());

				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is a right Either");
			}
		};
	}

	/**
	 * Matches an Either that is a Right (success), with the given value
	 *
	 * @param expectedValue The expected value contained in the right
	 * @param <T>           The type contained in the Either
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static <T> TypeSafeDiagnosingMatcher<Either<?, T>> isRight(@NotNull T expectedValue) {
		return new TypeSafeDiagnosingMatcher<Either<?, T>>() {
			@Override
			protected boolean matchesSafely(Either<?, T> subject, Description mismatchDescription) {
				if (subject.isRight()) {
					T actualValue = subject.get();

					if (actualValue.equals(expectedValue)) {
						return true;
					} else {
						mismatchDescription.appendText("is a right Either, with value ").appendValue(actualValue);
					}
				} else {
					mismatchDescription.appendText("is a left Either, with value ").appendValue(subject.getLeft());
				}

				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is a right Either, with value ").appendValue(expectedValue);
			}
		};
	}

	/**
	 * Matches an Either that is a Right (success), with the given matcher
	 *
	 * @param matcher The matcher that the contents should adhere to
	 * @param <T>     The type contained in the Either
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static <T> TypeSafeDiagnosingMatcher<Either<?, T>> isRight(@NotNull Matcher<T> matcher) {
		return new TypeSafeDiagnosingMatcher<Either<?, T>>() {
			@Override
			protected boolean matchesSafely(Either<?, T> subject, Description mismatchDescription) {
				if (subject.isRight()) {
					T actualValue = subject.get();

					if (matcher.matches(actualValue)) {
						return true;
					} else {
						mismatchDescription.appendText("is a right Either, with value ").appendValue(actualValue)
								.appendText(" not matching because ");
						matcher.describeMismatch(actualValue, mismatchDescription);
					}
				} else {
					mismatchDescription.appendText("is a left Either, with value ").appendValue(subject.getLeft());
				}

				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is a right Either, matching ");
				matcher.describeTo(description);
			}
		};
	}

	/**
	 * Matches a right Either with a value matching the given Predicate
	 *
	 * @param predicateDescription Describes the predicate for user feedback
	 * @param predicate            The predicate to match
	 * @param <T>                  The type of value that should be in the Either
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static <T> TypeSafeDiagnosingMatcher<Either<?, T>> isRight(
			@NotNull String predicateDescription, @NotNull Predicate<T> predicate) {
		return new TypeSafeDiagnosingMatcher<Either<?, T>>() {
			@Override
			protected boolean matchesSafely(Either<?, T> subject, Description mismatchDescription) {
				if (subject.isRight()) {
					T actualValue = subject.get();

					if (predicate.test(actualValue)) {
						return true;
					} else {
						mismatchDescription.appendText("is a right Either, with a value not matching ")
								.appendValue(predicateDescription).appendText(", because the value is equal to ")
								.appendValue(actualValue);
					}
				} else {
					mismatchDescription.appendText("is a left Either, with value ").appendValue(subject.getLeft());
				}

				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is a right Either, with a value matching ").appendValue(predicateDescription);
			}
		};
	}
	// endregion

	// region Matchers for Try<T>

	/**
	 * Matches a Try that is a success
	 *
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static TypeSafeDiagnosingMatcher<Try<?>> isSuccess() {
		return new TypeSafeDiagnosingMatcher<Try<?>>() {
			@Override
			protected boolean matchesSafely(Try<?> subject, Description mismatchDescription) {
				if (subject.isSuccess()) {
					return true;
				}

				mismatchDescription.appendText("is a failure, with exception of type ")
						.appendValue(subject.getCause().getClass());

				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is a success");
			}
		};
	}

	/**
	 * A matcher for Try that is a success, and contains the given value
	 *
	 * @param expectedValue The expected value
	 * @param <T>           The type of expected value
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static <T> TypeSafeDiagnosingMatcher<Try<T>> isSuccess(@NotNull T expectedValue) {
		return new TypeSafeDiagnosingMatcher<Try<T>>() {
			@Override
			protected boolean matchesSafely(Try<T> subject, Description mismatchDescription) {
				if (subject.isSuccess()) {
					T actualValue = subject.get();

					if (expectedValue.equals(actualValue)) {
						return true;
					}

					mismatchDescription.appendText("is a success, with value ")
							.appendValue(actualValue);
				} else {
					mismatchDescription.appendText("is a failure, with exception of type ")
							.appendValue(subject.getCause().getClass());
				}

				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is a success, with value ").appendValue(expectedValue);
			}
		};
	}

	/**
	 * A matcher for Try that is a success, whose contents match the given matcher
	 *
	 * @param matcher The matcher that the contents should adhere to
	 * @param <T>     The type of expected value
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static <T> TypeSafeDiagnosingMatcher<Try<T>> isSuccess(@NotNull Matcher<T> matcher) {
		return new TypeSafeDiagnosingMatcher<Try<T>>() {
			@Override
			protected boolean matchesSafely(Try<T> subject, Description mismatchDescription) {
				if (subject.isSuccess()) {
					T actualValue = subject.get();

					if (matcher.matches(actualValue)) {
						return true;
					}

					mismatchDescription.appendText("is a success, with value ")
							.appendValue(actualValue)
							.appendText(" not matching because ");
					matcher.describeMismatch(actualValue, mismatchDescription);
				} else {
					mismatchDescription.appendText("is a failure, with exception of type ")
							.appendValue(subject.getCause().getClass());
				}

				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is a success, matching ");
				matcher.describeTo(description);
			}
		};
	}

	/**
	 * Matches a Try that is a success, whose value matches the given predicate
	 *
	 * @param predicateDescription Describes the predicate for user feedback
	 * @param predicate            The predicate to match
	 * @param <T>                  The type of value in the try
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static <T> TypeSafeDiagnosingMatcher<Try<T>> isSuccess(
			@NotNull String predicateDescription, @NotNull Predicate<T> predicate) {
		return new TypeSafeDiagnosingMatcher<Try<T>>() {
			@Override
			protected boolean matchesSafely(Try<T> subject, Description mismatchDescription) {
				if (subject.isSuccess()) {
					T actualValue = subject.get();

					if (predicate.test(actualValue)) {
						return true;
					}

					mismatchDescription.appendText("is a success, which does not match ")
							.appendValue(predicateDescription).appendText(", because the value is equal to ")
							.appendValue(actualValue);
				} else {
					mismatchDescription.appendText("is a failure, with exception of type ")
							.appendValue(subject.getCause().getClass());
				}

				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is a success, matching ").appendValue(predicateDescription);
			}
		};
	}

	/**
	 * Matches a Try that is a failure
	 *
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static TypeSafeDiagnosingMatcher<Try<?>> isFailure() {
		return new TypeSafeDiagnosingMatcher<Try<?>>() {
			@Override
			protected boolean matchesSafely(Try<?> subject, Description mismatchDescription) {
				if (subject.isFailure()) {
					return true;
				}

				mismatchDescription.appendText("is a success, with value ")
						.appendValue(subject.get());

				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is a failure");
			}
		};
	}

	/**
	 * Matches a Try that is a failure, containing the given exception
	 *
	 * @param expectedClass The class of the exception contained in the Try
	 * @param <T>           The type of Throwable
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static <T extends Throwable> TypeSafeDiagnosingMatcher<Try<?>> isFailure(@NotNull Class<T> expectedClass) {
		return new TypeSafeDiagnosingMatcher<Try<?>>() {
			@Override
			protected boolean matchesSafely(Try<?> subject, Description mismatchDescription) {
				if (subject.isFailure()) {
					Throwable actualValue = subject.getCause();

					Class<? extends Throwable> actualClass = actualValue.getClass();
					if (expectedClass.isAssignableFrom(actualClass)) {
						return true;
					} else {
						mismatchDescription.appendText("is a failure, with exception of type ")
								.appendValue(actualClass.getName());
					}
				} else {
					mismatchDescription.appendText("is a success, with value ")
							.appendValue(subject.get());
				}

				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is a failure, with exception of type ").appendValue(expectedClass.getName());
			}
		};
	}

	/**
	 * Matches a Try that is a failure, containing the given exception
	 *
	 * @param matcher A matcher the throwable should adhere to
	 * @param <T>     The type of Throwable
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static <T extends Throwable> TypeSafeDiagnosingMatcher<Try<?>> isFailure(@NotNull Matcher<T> matcher) {
		return new TypeSafeDiagnosingMatcher<Try<?>>() {
			@Override
			protected boolean matchesSafely(Try<?> subject, Description mismatchDescription) {
				if (subject.isFailure()) {
					Throwable actualValue = subject.getCause();

					if (matcher.matches(actualValue)) {
						return true;
					} else {
						mismatchDescription.appendText("is a failure, with exception of type ")
								.appendValue(actualValue.getClass().getName())
								.appendText(" not matching because ");
						matcher.describeMismatch(actualValue, mismatchDescription);
					}
				} else {
					mismatchDescription.appendText("is a success, with value ")
							.appendValue(subject.get());
				}

				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is a failure, matching ");
				matcher.describeTo(description);
			}
		};
	}

	/**
	 * Matches a try that is a failure, the contained throwable of which matches the given predicate
	 *
	 * @param predicateDescription The description of the predicate
	 * @param predicate            The predicate
	 * @return A HamCrest matcher
	 */
	@NotNull
	public static TypeSafeDiagnosingMatcher<Try<?>> isFailure(
			@NotNull String predicateDescription, @NotNull Predicate<Throwable> predicate) {
		return new TypeSafeDiagnosingMatcher<Try<?>>() {
			@Override
			protected boolean matchesSafely(Try<?> subject, Description mismatchDescription) {
				if (subject.isFailure()) {
					Throwable actualValue = subject.getCause();

					Class<? extends Throwable> actualClass = actualValue.getClass();
					if (predicate.test(actualValue)) {
						return true;
					} else {
						mismatchDescription.appendText("is a failure, not matching ").appendValue(predicateDescription)
								.appendText(", and exception ").appendValue(actualClass.getName());
					}
				} else {
					mismatchDescription.appendText("is a success, with value ")
							.appendValue(subject.get());
				}

				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("is a failure, with throwable matching ").appendValue(predicateDescription);
			}
		};
	}

	// endregion

	// region Matchers for Future<T>

	/**
	 * Matches a future with any value
	 *
	 * @param <T> The type of object returned by the future
	 * @return A matcher
	 */
	@NotNull
	public static <T> FutureMatcher<T, ?> isFuture() {
		return new FutureMatcher.Success<>();
	}

	/**
	 * Matches a future with the given value
	 *
	 * @param expectedValue The expected value
	 * @param <T>           The type of object returned by the future
	 * @return A matcher
	 */
	@NotNull
	public static <T> FutureMatcher<T, ?> isFuture(@NotNull T expectedValue) {
		return new FutureMatcher.SuccessWithValue<>(expectedValue);
	}

	/**
	 * Matches a future whose result matches a given matcher
	 *
	 * @param matcher The matcher the result should adhere to
	 * @param <T>           The type of object returned by the future
	 * @return A matcher
	 */
	@NotNull
	public static <T> FutureMatcher<T, ?> isFuture(@NotNull Matcher<T> matcher) {
		return new FutureMatcher.SuccessMatching<>(matcher);
	}

	/**
	 * Matches a future whose result matches the given predicate
	 *
	 * @param predicateDescription A human-readable description of the predicate
	 * @param predicate            The predicate
	 * @param <T>                  The type of value returned by the future
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static <T> FutureMatcher<T, ?> isFutureMatching(
			@NotNull String predicateDescription, @NotNull Predicate<T> predicate) {
		return new FutureMatcher.SuccessMatchingPredicate<>(predicateDescription, predicate);
	}

	/**
	 * Matches a Future that fails
	 *
	 * @param <T> The type of value that would be returned by the future if it succeeded instead
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static <T> FutureMatcher<T, ?> isFailedFuture() {
		return new FutureMatcher.Failure<>();

	}

	/**
	 * Matches a Future that fails, due to the given exception type
	 *
	 * @param expectedException The exception that we expect to cause the future to fail
	 * @param <T>               The type of value that would be returned by the future if it succeeded instead
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static <T> FutureMatcher.FailureOfType<T> isFailedFuture(
			@NotNull Class<? extends Throwable> expectedException) {
		return new FutureMatcher.FailureOfType<>(expectedException);
	}

	/**
	 * Matches a Future that fails, due to the given exception type
	 *
	 * @param matcher The matcher the exception should adhere to
	 * @param <T>               The type of value that would be returned by the future if it succeeded instead
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static <T> FutureMatcher.FailureMatching<T> isFailedFuture(
			@NotNull Matcher<? extends Throwable> matcher) {
		return new FutureMatcher.FailureMatching<>(matcher);
	}

	/**
	 * Matches a Future that fails, due to an exception matching the given predicate
	 *
	 * @param predicateDescription A human-readable description of the predicate
	 * @param throwablePredicate   The predicate
	 * @param <T>                  The type of value that would have been returned by the Future if it had succeeded
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static <T> FutureMatcher.FailureMatchingPredicate<T> isFailedFutureMatching(
			@NotNull String predicateDescription, @NotNull Predicate<Throwable> throwablePredicate) {
		return new FutureMatcher.FailureMatchingPredicate<>(predicateDescription, throwablePredicate);
	}

	// endregion

	// region Matchers for Lazy<T>

	/**
	 * Matches a Lazy that yields any value
	 *
	 * @param <T> The type of value returned by the lazy
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static <T> LazyMatcher<T, ?> isLazy() {
		return new LazyMatcher.Bare<>();
	}

	/**
	 * Matches a Lazy that yields any value
	 *
	 * @param expectedValue The value the Lazy should yield
	 * @param <T>           The type of value returned by the lazy
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static <T> LazyMatcher<T, ?> isLazy(@NotNull T expectedValue) {
		return new LazyMatcher.Valued<>(expectedValue);
	}

	/**
	 * Matches a Lazy that yields any value
	 *
	 * @param matcher The matcher the lazily calculated value should adhere to
	 * @param <T>           The type of value returned by the lazy
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static <T> LazyMatcher<T, ?> isLazy(@NotNull Matcher<T> matcher) {
		return new LazyMatcher.Matching<>(matcher);
	}

	/**
	 * Matches a Lazy whose result matches the given predicate
	 *
	 * @param predicateDescription A human-readable description of the predicate
	 * @param predicate            The predicate
	 * @param <T>                  The type of value returned by the Lazy
	 * @return A Hamcrest matcher
	 */
	@NotNull
	public static <T> LazyMatcher<T, ?> isLazyMatching(
			@NotNull String predicateDescription, @NotNull Predicate<T> predicate) {
		return new LazyMatcher.MatchingPredicate<>(predicateDescription, predicate);
	}

	// endregion

	// region Internal

	/**
	 * Internal-use method for testing descriptions
	 *
	 * @param matcher The matcher to get the description of
	 * @return A String containing the Matcher's description
	 */
	static String descriptionOf(TypeSafeDiagnosingMatcher<?> matcher) {
		StringDescription description = new StringDescription();
		matcher.describeTo(description);
		return description.toString();
	}
	// endregion

}
