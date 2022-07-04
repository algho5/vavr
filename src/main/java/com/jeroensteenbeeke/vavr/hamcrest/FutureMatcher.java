package com.jeroensteenbeeke.vavr.hamcrest;

import io.vavr.concurrent.Future;
import io.vavr.control.Option;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Matcher for Vavr's Future
 *
 * @param <T> The type of value calculated by the future
 */
public class FutureMatcher<T> extends TypeSafeDiagnosingMatcher<Future<T>> {
	private final T expectedValue;
	private final long timeoutAmount;
	private final TimeUnit timeoutUnit;

	private final Class<? extends Throwable> expectedException;

	/**
	 * Constructor
	 */
	FutureMatcher() {
		this(null, 0, null, null);
	}

	private FutureMatcher(
			@Nullable T expectedValue, long timeoutAmount, @Nullable TimeUnit timeoutUnit,
			@Nullable Class<? extends Throwable> expectedException) {
		this.expectedValue = expectedValue;
		this.timeoutAmount = timeoutAmount;
		this.timeoutUnit = timeoutUnit;
		this.expectedException = expectedException;
	}

	/**
	 * Changes the expected outcome of the Future to failure
	 *
	 * @param expectedException The exception the Future should fail with
	 * @return A new matcher
	 */
	public FutureMatcher<T> thatFailsWith(@NotNull Class<? extends Throwable> expectedException) {
		return new FutureMatcher<>(expectedValue, timeoutAmount, timeoutUnit, expectedException);
	}

	/**
	 * Creates a new matcher with the given expected value
	 *
	 * @param expectedValue The expected value
	 * @return A new matcher
	 */
	public FutureMatcher<T> withExpectedValue(@NotNull T expectedValue) {
		return new FutureMatcher<>(expectedValue, timeoutAmount, timeoutUnit, expectedException);
	}

	/**
	 * Creates a new matcher with the given timeout
	 *
	 * @param timeoutAmount The number of units to wait
	 * @param timeoutUnit   The type of unit to wait
	 * @return A new matcher
	 */
	public FutureMatcher<T> withTimeout(long timeoutAmount, @NotNull TimeUnit timeoutUnit) {
		return new FutureMatcher<>(expectedValue, timeoutAmount, timeoutUnit, expectedException);
	}

	@Override
	protected boolean matchesSafely(Future<T> providedFuture, Description mismatchDescription) {

		Future<T> awaitedFuture;

		if (timeoutUnit == null) {
			// Ignore timeout
			awaitedFuture = providedFuture.await();
		} else if (timeoutAmount <= 0) {
			// Someone didn't read the manual
			mismatchDescription.appendText("invalid parameter timeoutAmount, must be positive, but is ").appendValue(timeoutAmount);
			return false;
		}  else {
			awaitedFuture = providedFuture.await(timeoutAmount, timeoutUnit);
		}

		mismatchDescription.appendText("is a Future");

		if (awaitedFuture.isSuccess()) {
			mismatchDescription.appendText(", that succeeds");
		} else {
			mismatchDescription.appendText(", that fails");
		}

		boolean expectedSuccess = expectedException == null;

		if (expectedSuccess == awaitedFuture.isSuccess()) {
			if (expectedSuccess) {
				T actualValue = awaitedFuture.get();

				mismatchDescription.appendText(", with value ");
				mismatchDescription.appendValue(actualValue);

				if (expectedValue == null) {
					return true;
				}

				if (expectedValue.equals(actualValue)) {
					return true;
				}
			} else {
				Option<Throwable> cause = awaitedFuture.getCause();

				if (cause.isDefined()) {
					Throwable t = cause.get();

					if (expectedException.isInstance(t)) {
						return true;
					}
				} else {
					mismatchDescription.appendText(", but has no defined failure cause");
				}
			}
		}

		if (awaitedFuture.isFailure()) {
			Option<Throwable> cause = awaitedFuture.getCause();

			if (cause.isDefined()) {
				Throwable t = cause.get();
				if (t instanceof TimeoutException) {
					mismatchDescription.appendText(" by exceeding timeout");
				} else {
					mismatchDescription.appendText(", but threw exception of type ")
							.appendValue(t.getClass().getName());
					if (t.getMessage() != null) {
						mismatchDescription.appendText(", with message ").appendValue(t.getMessage());
					}
				}
			}
		}
		return false;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("is a Future");

		if (expectedException == null) {
			description.appendText(", that succeeds");
		} else {
			description.appendText(", that fails, with exception of type ");
			description.appendValue(expectedException.getName());
		}

		if (expectedValue != null) {
			description.appendText(", with value ");
			description.appendValue(expectedValue);
		}

		if (timeoutUnit != null) {
			description.appendText(", that yields a responds within ");
			description.appendText(Long.toString(timeoutAmount));
			description.appendText(" ");
			description.appendText(timeoutUnit.toString().toLowerCase());

		}
	}
}
