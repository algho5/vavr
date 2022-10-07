package com.jeroensteenbeeke.vavr.hamcrest;

import io.vavr.concurrent.Future;
import io.vavr.control.Option;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

/**
 * Matcher for Vavr's Future
 *
 * @param <T> The type of value calculated by the future
 * @param <F> The type of the implementing subclass
 */
public abstract class FutureMatcher<T, F extends FutureMatcher<T, F>> extends TypeSafeDiagnosingMatcher<Future<T>> {
	protected final long timeoutAmount;
	protected final TimeUnit timeoutUnit;

	/**
	 * Constructor
	 *
	 * @param timeoutAmount The number of time units to wait for execution to complete
	 * @param timeoutUnit The type of time unit to wait for execution to complete
	 */
	protected FutureMatcher(long timeoutAmount, @Nullable TimeUnit timeoutUnit) {
		this.timeoutAmount = timeoutAmount;
		this.timeoutUnit = timeoutUnit;
	}

	/**
	 * Creates a new instance of the current class
	 *
	 * @param timeoutAmount The timeout amount (default 0)
	 * @param timeoutUnit   The unit of timeouts (default null)
	 * @return A new instance of the current class
	 */
	@NotNull
	protected abstract F newInstance(long timeoutAmount, @Nullable TimeUnit timeoutUnit);

	/**
	 * Creates a new matcher with the given timeout
	 *
	 * @param timeoutAmount The number of units to wait
	 * @param timeoutUnit   The type of unit to wait
	 * @return A new matcher
	 */
	@NotNull
	public F withTimeout(long timeoutAmount, @NotNull TimeUnit timeoutUnit) {
		return newInstance(timeoutAmount, timeoutUnit);
	}

	/**
	 * Determines whether or not the given future adheres to the test condition specified
	 *
	 * @param awaitedFuture       The future that has already been subjected to any applicable timeouts
	 * @param mismatchDescription The description to give as feedback to the developers
	 * @return {@code true} if the test succeeds, {@code false} otherwise
	 */
	protected abstract boolean matchesFutureSafely(
			@NotNull Future<T> awaitedFuture, @NotNull Description mismatchDescription);

	@Override
	protected final boolean matchesSafely(@NotNull Future<T> providedFuture, @NotNull Description mismatchDescription) {

		Future<T> awaitedFuture;

		if (timeoutUnit == null) {
			// Ignore timeout
			awaitedFuture = providedFuture.await();
		} else if (timeoutAmount <= 0) {
			// Someone didn't read the manual
			mismatchDescription.appendText("invalid parameter timeoutAmount, must be positive, but is ")
					.appendValue(timeoutAmount);
			return false;
		} else {
			awaitedFuture = providedFuture.await(timeoutAmount, timeoutUnit);
		}

		mismatchDescription.appendText("is a Future");

		if (awaitedFuture.isFailure() && awaitedFuture.getCause().filter(TimeoutException.class::isInstance).isDefined()) {
			mismatchDescription.appendText(", that fails by exceeding timeout");
			return false;
		}


		return matchesFutureSafely(awaitedFuture, mismatchDescription);
	}

	@Override
	public void describeTo(@NotNull Description description) {
		description.appendText("is a Future");

		if (timeoutAmount > 0L && timeoutUnit != null) {
			description.appendText(", that completes within ");
			description.appendText(Long.toString(timeoutAmount));
			description.appendText(" ");
			description.appendText(timeoutUnit.toString().toLowerCase());
		}
	}

	protected final void describeFailure(@NotNull Future<T> awaitedFuture, @NotNull Description description) {
		description.appendText(", that fails");

		Option<Throwable> cause = awaitedFuture.getCause();

		if (cause.isDefined()) {
			Throwable throwable = cause.get();

			description.appendText(", with exception of type ");
			description.appendValue(throwable.getClass().getName());

			if (throwable.getMessage() != null) {
				description.appendText(", with message ");
				description.appendValue(throwable.getMessage());
			}
		} else {
			description.appendText(", but has no defined failure cause");
		}
	}

	protected final void describeSuccess(@NotNull Future<T> awaitedFuture, @NotNull Description mismatchDescription) {
		mismatchDescription.appendText(", that succeeds");
		mismatchDescription.appendText(", and yields value ");
		mismatchDescription.appendValue(awaitedFuture.get());
	}

	/**
	 * Future Matcher that represents a failure by an expected exception type
	 *
	 * @param <T> The type of value returned by the future had it been a success
	 */
	public static class Failure<T> extends FutureMatcher<T, Failure<T>> {
		Failure() {
			this(0L, null);
		}

		private Failure(long timeoutAmount, @Nullable TimeUnit timeoutUnit) {
			super(timeoutAmount, timeoutUnit);
		}

		@Override
		@NotNull
		protected Failure<T> newInstance(long timeoutAmount, @Nullable TimeUnit timeoutUnit) {
			return new Failure<>(timeoutAmount, timeoutUnit);
		}

		@Override
		protected boolean matchesFutureSafely(
				@NotNull Future<T> awaitedFuture, @NotNull Description mismatchDescription) {
			if (awaitedFuture.isFailure()) {
				return true;
			} else {
				mismatchDescription.appendText(", that succeeds");
				mismatchDescription.appendText(", and yields value ");
				mismatchDescription.appendValue(awaitedFuture.get());
			}

			return false;
		}

		@Override
		public void describeTo(@NotNull Description description) {
			super.describeTo(description);
			description.appendText(", that fails");
		}
	}

	/**
	 * Future Matcher that represents a failure by an expected exception type
	 *
	 * @param <T> The type of value returned by the future had it been a success
	 */
	public static class FailureOfType<T> extends FutureMatcher<T, FailureOfType<T>> {
		private final Class<? extends Throwable> expectedException;

		private final String expectedExceptionMessage;

		FailureOfType(@NotNull Class<? extends Throwable> expectedException) {
			this(0L, null, expectedException, null);
		}

		private FailureOfType(
				long timeoutAmount, @Nullable TimeUnit timeoutUnit,
				@NotNull Class<? extends Throwable> expectedException,
				@Nullable String expectedExceptionMessage) {
			super(timeoutAmount, timeoutUnit);
			this.expectedException = expectedException;
			this.expectedExceptionMessage = expectedExceptionMessage;
		}

		@Override
		@NotNull
		protected FailureOfType<T> newInstance(long timeoutAmount, @Nullable TimeUnit timeoutUnit) {
			return new FailureOfType<>(timeoutAmount, timeoutUnit, expectedException, expectedExceptionMessage);
		}

		/**
		 * Yields a new matcher that requires the exception message to be equal to the given value
		 * @param expectedExceptionMessage The Exception message
		 * @return A new matcher
		 */
		@NotNull
		public FailureOfType<T> withMessage(@NotNull String expectedExceptionMessage) {
			return new FailureOfType<>(timeoutAmount, timeoutUnit, expectedException, expectedExceptionMessage);
		}

		@Override
		protected boolean matchesFutureSafely(
				@NotNull Future<T> awaitedFuture, @NotNull Description mismatchDescription) {
			if (awaitedFuture.isFailure()) {
				describeFailure(awaitedFuture, mismatchDescription);

				Option<Throwable> cause = awaitedFuture.getCause();
				if (cause.isDefined()) {
					Throwable throwable = cause.get();
					if (expectedException.isInstance(throwable)) {
						if (expectedExceptionMessage == null) {
							// Don't check message
							return true;
						} else {
							String message = throwable.getMessage();
							return expectedExceptionMessage.equals(message);
						}
					}
				}
			} else {
				describeSuccess(awaitedFuture, mismatchDescription);
			}

			return false;
		}

		@Override
		public void describeTo(@NotNull Description description) {
			super.describeTo(description);
			description.appendText(", that fails, with exception of type ");
			description.appendValue(expectedException.getName());

			if (expectedExceptionMessage != null) {
				description.appendText(" and message ");
				description.appendValue(expectedExceptionMessage);
			}
		}
	}

	/**
	 * Future Matcher that represents a failure by an expected exception type
	 *
	 * @param <T> The type of value returned by the future had it been a success
	 */
	public static class FailureMatching<T> extends FutureMatcher<T, FailureMatching<T>> {
		private final Matcher<? extends Throwable> matcher;

		FailureMatching(@NotNull Matcher<? extends Throwable> matcher) {
			this(0L, null, matcher);
		}

		private FailureMatching(
				long timeoutAmount, @Nullable TimeUnit timeoutUnit,
				@NotNull Matcher<? extends Throwable> matcher) {
			super(timeoutAmount, timeoutUnit);
			this.matcher = matcher;
		}

		@Override
		@NotNull
		protected FailureMatching<T> newInstance(long timeoutAmount, @Nullable TimeUnit timeoutUnit) {
			return new FailureMatching<>(timeoutAmount, timeoutUnit, matcher);
		}

		@Override
		protected boolean matchesFutureSafely(
				@NotNull Future<T> awaitedFuture, @NotNull Description mismatchDescription) {
			if (awaitedFuture.isFailure()) {
				describeFailure(awaitedFuture, mismatchDescription);

				Option<Throwable> cause = awaitedFuture.getCause();
				if (cause.isDefined()) {
					Throwable throwable = cause.get();
					if (matcher.matches(throwable)) {
						return true;
					}

					mismatchDescription.appendText(", that fails, because ");
					matcher.describeMismatch(throwable, mismatchDescription);
				}
			} else {
				describeSuccess(awaitedFuture, mismatchDescription);
			}

			return false;
		}

		@Override
		public void describeTo(@NotNull Description description) {
			super.describeTo(description);
			description.appendText(", that fails, with exception matching ");
			matcher.describeTo(description);
		}
	}

	/**
	 * Future Matcher that matches a failed future that matches a given predicate
	 * @param <T> The type of value returned by the future if it had succeeded
	 */
	public static class FailureMatchingPredicate<T> extends FutureMatcher<T, FailureMatchingPredicate<T>> {
		private final String predicateDescription;

		private final Predicate<Throwable> throwablePredicate;

		FailureMatchingPredicate(String predicateDescription, Predicate<Throwable> throwablePredicate) {
			this(0, null, predicateDescription, throwablePredicate);
		}

		private FailureMatchingPredicate(
				long timeoutAmount, @Nullable TimeUnit timeoutUnit, String predicateDescription,
				Predicate<Throwable> throwablePredicate) {
			super(timeoutAmount, timeoutUnit);
			this.predicateDescription = predicateDescription;
			this.throwablePredicate = throwablePredicate;
		}

		@Override
		@NotNull
		protected FailureMatchingPredicate<T> newInstance(long timeoutAmount, @Nullable TimeUnit timeoutUnit) {
			return new FailureMatchingPredicate<>(timeoutAmount, timeoutUnit, predicateDescription, throwablePredicate);
		}

		@Override
		protected boolean matchesFutureSafely(
				@NotNull Future<T> awaitedFuture, @NotNull Description mismatchDescription) {
			if (awaitedFuture.isFailure()) {
				Option<Throwable> cause = awaitedFuture.getCause();
				if (cause.isDefined()) {
					Throwable throwable = cause.get();
					if (throwablePredicate.test(throwable)) {
						return true;
					}

					mismatchDescription.appendText(", that fails, with exception not matching predicate ");
					mismatchDescription.appendValue(predicateDescription);
				} else {
					mismatchDescription.appendText(", that fails, but has no defined failure cause");
				}
			} else {
				describeSuccess(awaitedFuture, mismatchDescription);
			}

			return false;
		}

		@Override
		public void describeTo(@NotNull Description description) {
			super.describeTo(description);
			description.appendText(", that fails, with exception matching predicate ");
			description.appendValue(predicateDescription);
		}
	}

	/**
	 * Future Matcher that represents a success
	 * @param <T> The type of value returned by the future
	 */
	public static class Success<T> extends FutureMatcher<T, Success<T>> {

		Success() {
			this(0L, null);
		}

		private Success(long timeoutAmount, @Nullable TimeUnit timeoutUnit) {
			super(timeoutAmount, timeoutUnit);
		}

		@Override
		@NotNull
		protected Success<T> newInstance(long timeoutAmount, @Nullable TimeUnit timeoutUnit) {
			return new Success<>(timeoutAmount, timeoutUnit);
		}

		@Override
		protected boolean matchesFutureSafely(
				@NotNull Future<T> awaitedFuture, @NotNull Description mismatchDescription) {
			if (awaitedFuture.isSuccess()) {
				return true;
			} else {
				describeFailure(awaitedFuture, mismatchDescription);
			}

			return false;
		}

		@Override
		public void describeTo(@NotNull Description description) {
			super.describeTo(description);
			description.appendText(", that succeeds");
		}
	}



	/**
	 * Future Matcher that represents a success, with the indicated value
	 * @param <T> The type of value returned by the future
	 */
	public static class SuccessWithValue<T> extends FutureMatcher<T, SuccessWithValue<T>> {
		private final T expectedValue;

		SuccessWithValue(@NotNull T expectedValue) {
			this(0L, null, expectedValue);
		}

		private SuccessWithValue(long timeoutAmount, @Nullable TimeUnit timeoutUnit, @NotNull T expectedValue) {
			super(timeoutAmount, timeoutUnit);
			this.expectedValue = expectedValue;
		}

		@Override
		@NotNull
		protected FutureMatcher.SuccessWithValue<T> newInstance(long timeoutAmount, @Nullable TimeUnit timeoutUnit) {
			return new SuccessWithValue<>(timeoutAmount, timeoutUnit, expectedValue);
		}

		@Override
		protected boolean matchesFutureSafely(
				@NotNull Future<T> awaitedFuture, @NotNull Description mismatchDescription) {
			if (awaitedFuture.isSuccess()) {

				T actualValue = awaitedFuture.get();

				if (expectedValue.equals(actualValue)) {
					return true;
				}

				mismatchDescription.appendText(", that succeeds, with value ");
				mismatchDescription.appendValue(actualValue);

			} else {
				describeFailure(awaitedFuture, mismatchDescription);
			}


			return false;
		}

		@Override
		public void describeTo(@NotNull Description description) {
			super.describeTo(description);
			description.appendText(", that succeeds, with value ");
			description.appendValue(expectedValue);
		}
	}

	/**
	 * Future Matcher that represents a success, whose value matches another matcher
	 * @param <T> The type of value returned by the future
	 */
	public static class SuccessMatching<T> extends FutureMatcher<T, SuccessMatching<T>> {
		private final Matcher<T> matcher;

		SuccessMatching(@NotNull Matcher<T> matcher) {
			this(0L, null, matcher);
		}

		private SuccessMatching(long timeoutAmount, @Nullable TimeUnit timeoutUnit, @NotNull Matcher<T> matcher) {
			super(timeoutAmount, timeoutUnit);
			this.matcher = matcher;
		}

		@Override
		@NotNull
		protected FutureMatcher.SuccessMatching<T> newInstance(long timeoutAmount, @Nullable TimeUnit timeoutUnit) {
			return new SuccessMatching<>(timeoutAmount, timeoutUnit, matcher);
		}

		@Override
		protected boolean matchesFutureSafely(
				@NotNull Future<T> awaitedFuture, @NotNull Description mismatchDescription) {
			if (awaitedFuture.isSuccess()) {

				T actualValue = awaitedFuture.get();

				if (matcher.matches(actualValue)) {
					return true;
				}

				mismatchDescription.appendText(", that succeeds, with value ");
				mismatchDescription.appendValue(actualValue);
				mismatchDescription.appendText(" not matching because ");
				matcher.describeMismatch(actualValue, mismatchDescription);

			} else {
				describeFailure(awaitedFuture, mismatchDescription);
			}


			return false;
		}

		@Override
		public void describeTo(@NotNull Description description) {
			super.describeTo(description);
			description.appendText(", that succeeds, with value matching ");
			matcher.describeTo(description);
		}
	}

	/**
	 * Future Matcher that represents a success, with the indicated value
	 * @param <T> The type of value returned by the future
	 */
	public static class SuccessMatchingPredicate<T> extends FutureMatcher<T, SuccessMatchingPredicate<T>> {
		private final String predicateDescription;

		private final Predicate<T> valuePredicate;

		SuccessMatchingPredicate(@NotNull String predicateDescription, @NotNull Predicate<T> valuePredicate) {
			this(0L, null, predicateDescription, valuePredicate);
		}

		private SuccessMatchingPredicate(long timeoutAmount, @Nullable TimeUnit timeoutUnit, @NotNull String predicateDescription, @NotNull Predicate<T> valuePredicate) {
			super(timeoutAmount, timeoutUnit);
			this.predicateDescription = predicateDescription;
			this.valuePredicate = valuePredicate;
		}

		@Override
		@NotNull
		protected FutureMatcher.SuccessMatchingPredicate<T> newInstance(long timeoutAmount, @Nullable TimeUnit timeoutUnit) {
			return new SuccessMatchingPredicate<>(timeoutAmount, timeoutUnit, predicateDescription, valuePredicate);
		}

		@Override
		protected boolean matchesFutureSafely(
				@NotNull Future<T> awaitedFuture, @NotNull Description mismatchDescription) {
			if (awaitedFuture.isSuccess()) {

				T actualValue = awaitedFuture.get();

				if (valuePredicate.test(actualValue)) {
					return true;
				}

				mismatchDescription.appendText(", that succeeds, with value ");
				mismatchDescription.appendValue(actualValue);
				mismatchDescription.appendText(" not satisfying ");
				mismatchDescription.appendValue(predicateDescription);
			} else {
				describeFailure(awaitedFuture, mismatchDescription);
			}


			return false;
		}

		@Override
		public void describeTo(@NotNull Description description) {
			super.describeTo(description);
			description.appendText(", that succeeds, with value matching predicate ");
			description.appendValue(predicateDescription);
		}
	}
}
