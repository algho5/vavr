package com.jeroensteenbeeke.vavr.hamcrest;

import io.vavr.Lazy;
import io.vavr.concurrent.Future;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static com.jeroensteenbeeke.vavr.hamcrest.VavrMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

public class ExamplesTest {
	@Test
	void examples() {
		// Option
		assertThat(Option.none(), isNone());
		assertThat(Option.none(), isEmptyOption());
		assertThat(Option.some("A"), isSome());
		assertThat(Option.some("A"), isSome("A"));
		assertThat(Option.some("A"), isSome(equalTo("A")));
		assertThat(Option.some("A"), isDefinedOption());
		assertThat(Option.some("A"), isDefinedOption("A"));
		assertThat(Option.some("A"), isDefinedOption(equalTo("A")));
		assertThat(Option.some("A"), isDefinedOption("contains only alphabetic letters", s -> s.matches("^[a-zA-Z]*$")));

		// Either
		assertThat(Either.left("Error"), isLeft());
		assertThat(Either.left("Error"), isLeft("Error"));
		assertThat(Either.left("Error"), isLeft(equalTo("Error")));
		assertThat(Either.left("Error"), isLeft("Starts with an E", s -> s.startsWith("E")));
		assertThat(Either.right("Success"), isRight());
		assertThat(Either.right("Success"), isRight("Success"));
		assertThat(Either.right("Success"), isRight(equalTo("Success")));
		assertThat(Either.right("Success"), isRight("Starts with an S", s -> s.startsWith("S")));

		// Try
		assertThat(Try.success("S"), isSuccess());
		assertThat(Try.success("S"), isSuccess("S"));
		assertThat(Try.success("S"), isSuccess(equalTo("S")));
		assertThat(Try.success("S"), isSuccess("Starts with an S", v -> v.startsWith("S")));

		assertThat(Try.failure(new RuntimeException()), isFailure());
		assertThat(Try.failure(new IllegalStateException()), isFailure(IllegalStateException.class));
		assertThat(Try.failure(new IllegalStateException()), isFailure(instanceOf(IllegalStateException.class)));
		assertThat(Try.failure(new IllegalArgumentException()), isFailure("Is a runtime exception", t -> t instanceof RuntimeException));

		// Future
		assertThat(Future.of(() -> "A"), isFuture());
		assertThat(Future.of(() -> "A"), isFuture("A"));
		assertThat(Future.of(() -> "A"), isFuture(equalTo("A")));
		assertThat(Future.of(() -> "A"), isFuture("A").withTimeout(1, TimeUnit.SECONDS));
		assertThat(Future.of(() -> 5), VavrMatchers. <Integer> isFutureMatching("Value greater than or equal to 5", v -> v >= 5));

		assertThat(Future.of(() -> {
			throw new IllegalStateException("This is an error");
		}), isFailedFuture());
		assertThat(Future.of(() -> {
			throw new IllegalStateException("This is an error");
		}), isFailedFuture(IllegalStateException.class));
		assertThat(Future.of(() -> {
			throw new IllegalStateException("This is an error");
		}), isFailedFuture(IllegalStateException.class).withMessage("This is an error"));
		assertThat(Future.of(() -> {
			throw new IllegalStateException("This is an error");
		}), isFailedFuture(instanceOf(IllegalStateException.class)));
		assertThat(Future.of(() -> {
			throw new IllegalStateException("This is an error");
		}), isFailedFuture(IllegalStateException.class).withMessage("This is an error")
				.withTimeout(1, TimeUnit.SECONDS));
		assertThat(Future.of(() -> {
			throw new IllegalStateException("Illegal State");
		}), VavrMatchers.<Integer>isFailedFutureMatching("Exception has message 'Illegal State'", t -> "Illegal State".equals(t.getMessage())));

		// Lazy
		assertThat(Lazy.of(() -> 5), isLazy());
		assertThat(Lazy.of(() -> 5), isLazy().withTimeout(1, TimeUnit.SECONDS));
		assertThat(Lazy.of(() -> 5), isLazy(5));
		assertThat(Lazy.of(() -> 5), isLazy(equalTo(5)));
		assertThat(Lazy.of(() -> 5), isLazy(5).withTimeout(1, TimeUnit.SECONDS));
		assertThat(Lazy.of(() -> 5), VavrMatchers. <Integer> isLazyMatching("== 5", v -> v == 5));

	}

}
