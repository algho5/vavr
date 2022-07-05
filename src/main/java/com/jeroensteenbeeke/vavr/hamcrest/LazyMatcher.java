package com.jeroensteenbeeke.vavr.hamcrest;

import io.vavr.Lazy;
import io.vavr.concurrent.Future;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Matcher for Lazy objects that yields objects of the given type
 *
 * @param <T> The type of return value
 * @param <L> The type of the implementing class
 */
public abstract class LazyMatcher<T, L extends LazyMatcher<T, L>> extends TypeSafeDiagnosingMatcher<Lazy<T>> {
	protected final long timeoutAmount;
	protected final TimeUnit timeoutUnit;

	/**
	 * Constructor
	 *
	 * @param timeoutAmount The number of time units to wait for execution to complete
	 * @param timeoutUnit   The type of time unit to wait for execution to complete
	 */
	protected LazyMatcher(long timeoutAmount, @Nullable TimeUnit timeoutUnit) {
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
	protected abstract L newInstance(long timeoutAmount, @Nullable TimeUnit timeoutUnit);

	/**
	 * Creates a new matcher with the given timeout
	 *
	 * @param timeoutAmount The number of units to wait
	 * @param timeoutUnit   The type of unit to wait
	 * @return A new matcher
	 */
	@NotNull
	public L withTimeout(long timeoutAmount, @NotNull TimeUnit timeoutUnit) {
		return newInstance(timeoutAmount, timeoutUnit);
	}

	@Override
	protected boolean matchesSafely(@NotNull Lazy<T> lazy, @NotNull Description mismatchDescription) {
		if (timeoutUnit == null) {
			// Ignore timeout
			mismatchDescription.appendText("is a Lazy");

			return matchesLazySafely(lazy, mismatchDescription);
		} else if (timeoutAmount <= 0) {
			// Someone didn't read the manual
			mismatchDescription.appendText("invalid parameter timeoutAmount, must be positive, but is ")
					.appendValue(timeoutAmount);
			return false;
		} else {
			mismatchDescription.appendText("is a Lazy");

			Future<Lazy<T>> awaited = Future.of(() -> {
				lazy.get();
				return lazy;
			}).await(timeoutAmount, timeoutUnit);

			if (awaited.isFailure()) {
				mismatchDescription.appendText(", that fails by exceeding timeout");
				return false;
			} else {
				return matchesLazySafely(awaited.get(), mismatchDescription);
			}
		}
	}

	@Override
	public void describeTo(@NotNull Description description) {
		description.appendText("is a Lazy");
		if (timeoutAmount > 0L && timeoutUnit != null) {
			description.appendText(", that completes within ");
			description.appendText(Long.toString(timeoutAmount));
			description.appendText(" ");
			description.appendText(timeoutUnit.toString().toLowerCase());
		}
	}

	/**
	 * Determines whether or not the given lazy adheres to the test condition specified
	 *
	 * @param lazy                The lazy that has already been subjected to any applicable timeouts
	 * @param mismatchDescription The description to give as feedback to the developers
	 * @return {@code true} if the test succeeds, {@code false} otherwise
	 */
	protected abstract boolean matchesLazySafely(@NotNull Lazy<T> lazy, @NotNull Description mismatchDescription);

	/**
	 * Matcher that just checks if the return value is a Lazy, but does not verify contents
	 *
	 * @param <T> The type of value yielded by the Lazy
	 */
	public static class Bare<T> extends LazyMatcher<T, Bare<T>> {
		/**
		 * Constructor
		 */
		Bare() {
			this(0, null);
		}

		/**
		 * Constructor
		 *
		 * @param timeoutAmount The amount of units to wait for a result
		 * @param timeoutUnit   The type of units to wait for a result
		 */
		private Bare(long timeoutAmount, @Nullable TimeUnit timeoutUnit) {
			super(timeoutAmount, timeoutUnit);
		}

		@Override
		@NotNull
		protected Bare<T> newInstance(long timeoutAmount, @Nullable TimeUnit timeoutUnit) {
			return new Bare<>(timeoutAmount, timeoutUnit);
		}

		@Override
		protected boolean matchesLazySafely(@NotNull Lazy<T> lazy, @NotNull Description mismatchDescription) {
			return true;
		}

	}

	/**
	 * Matcher that just checks if the return value is a Lazy, and checks if the yielded
	 * value is equal to a given value
	 *
	 * @param <T> The type of value yielded by the Lazy
	 */
	public static class Valued<T> extends LazyMatcher<T, Valued<T>> {
		private final T expectedValue;

		/**
		 * Constructor
		 *
		 * @param expectedValue The value expected
		 */
		public Valued(@NotNull T expectedValue) {
			this(0L, null, expectedValue);
		}

		/**
		 * Constructor
		 *
		 * @param timeoutAmount The amount of units to wait for a result
		 * @param timeoutUnit   The type of units to wait for a result
		 * @param expectedValue The value expected
		 */
		private Valued(long timeoutAmount, @Nullable TimeUnit timeoutUnit, @NotNull T expectedValue) {
			super(timeoutAmount, timeoutUnit);
			this.expectedValue = expectedValue;
		}

		@Override
		@NotNull
		protected Valued<T> newInstance(long timeoutAmount, @Nullable TimeUnit timeoutUnit) {
			return new Valued<>(timeoutAmount, timeoutUnit, expectedValue);
		}

		@Override
		protected boolean matchesLazySafely(@NotNull Lazy<T> lazy, @NotNull Description mismatchDescription) {
			T actualValue = lazy.get();

			if (actualValue.equals(expectedValue)) {
				return true;
			}

			mismatchDescription.appendText(", which yields value ");
			mismatchDescription.appendValue(actualValue);

			return false;
		}

		@Override
		public void describeTo(@NotNull Description description) {
			super.describeTo(description);
			description.appendText(", which yields value ");
			description.appendValue(expectedValue);
		}
	}

	/**
	 * Matcher that just checks if the return value is a Lazy, and checks if the
	 * value matches a given predicate
	 *
	 * @param <T> The type of value yielded by the Lazy
	 */
	public static class MatchingPredicate<T> extends LazyMatcher<T, MatchingPredicate<T>> {
		private final String predicateDescription;

		private final Predicate<T> valuePredicate;

		MatchingPredicate(@NotNull String predicateDescription, @NotNull Predicate<T> valuePredicate) {
			this(0L, null, predicateDescription, valuePredicate);
		}

		private MatchingPredicate(long timeoutAmount, @Nullable TimeUnit timeoutUnit, @NotNull String predicateDescription, @NotNull Predicate<T> valuePredicate) {
			super(timeoutAmount, timeoutUnit);
			this.predicateDescription = predicateDescription;
			this.valuePredicate = valuePredicate;
		}

		@Override
		@NotNull
		protected MatchingPredicate<T> newInstance(long timeoutAmount, @Nullable TimeUnit timeoutUnit) {
			return new MatchingPredicate<>(timeoutAmount, timeoutUnit, predicateDescription, valuePredicate);
		}

		@Override
		protected boolean matchesLazySafely(@NotNull Lazy<T> lazy, @NotNull Description mismatchDescription) {
			T actualValue = lazy.get();

			if (valuePredicate.test(actualValue)) {
				return true;
			}

			mismatchDescription.appendText(", which yields value ");
			mismatchDescription.appendValue(actualValue);
			mismatchDescription.appendText(", which does not satisfy ");
			mismatchDescription.appendValue(predicateDescription);

			return false;
		}

		@Override
		public void describeTo(@NotNull Description description) {
			super.describeTo(description);
			description.appendText(", which satisfies ");
			description.appendValue(predicateDescription);
		}
	}
}
