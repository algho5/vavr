package com.jeroensteenbeeke.vavr.hamcrest;

import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.jeroensteenbeeke.vavr.hamcrest.VavrMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class VavrMatchersTest {
	@Test
	void testOptionMatchers() {
		assertThat(Option.none(), isNone());
		assertThat(Option.some("A"), not(isNone()));
		assertThat(Option.none(), isEmptyOption());
		assertThat(Option.some("A"), not(isEmptyOption()));

		assertThat(Option.some("A"), isDefinedOption());
		assertThat(Option.none(), not(isDefinedOption()));
		assertThat(Option.some("A"), isDefinedOption("A"));
		assertThat(Option.some("B"), not(isDefinedOption("A")));
		assertThat(Option.none(), not(isDefinedOption("A")));

		assertThat(Option.some("A"), isSome());
		assertThat(Option.none(), not(isSome()));
		assertThat(Option.some("A"), isSome("A"));
		assertThat(Option.some("B"), not(isSome("A")));
		assertThat(Option.none(), not(isSome("A")));

		assertThat(Option.some("A"), isDefinedOption("contains only alphabetic letters", s -> s.matches("^[a-zA-Z]*$")));
		assertThat(Option.some("1"), not(isDefinedOption("contains only alphabetic letters", s -> s.matches("^[a-zA-Z]*$"))));
		assertThat(Option.<String> none(), not(isDefinedOption("contains only alphabetic letters", s -> s.matches("^[a-zA-Z]*$"))));
	}

	@Test
	void testEitherMatchers() {
		assertThat(Either.left("Error"), isLeft());
		assertThat(Either.right("Error"), not(isLeft()));

		assertThat(Either.left("Error"), isLeft("Error"));
		assertThat(Either.left("ErRoR"), not(isLeft("Error")));
		assertThat(Either.right("Error"), not(isLeft("Error")));
		assertThat(Either.left("Error"), isLeft("Starts with an E", s -> s.startsWith("E")));
		assertThat(Either.left("Arror"), not(isLeft("Starts with an E", s -> s.startsWith("E"))));
		assertThat(Either.<String,String> right("Error"), not(isLeft("Starts with an E", s -> s.startsWith("E"))));

		assertThat(Either.right("Error"), isRight());
		assertThat(Either.left("Error"), not(isRight()));

		assertThat(Either.right("Error"), isRight("Error"));
		assertThat(Either.right("ErRoR"), not(isRight("Error")));
		assertThat(Either.left("Error"), not(isRight("Error")));
		assertThat(Either.right("Error"), isRight("Starts with an E", s -> s.startsWith("E")));
		assertThat(Either.right("Arror"), not(isRight("Starts with an E", s -> s.startsWith("E"))));
		assertThat(Either.<String,String> left("Error"), not(isRight("Starts with an E", s -> s.startsWith("E"))));
	}

	@Test
	void testTryMatchers() {
		assertThat(Try.success("S"), isSuccess());
		assertThat(Try.failure(new RuntimeException()), not(isSuccess()));

		assertThat(Try.success("S"), isSuccess("S"));
		assertThat(Try.success("B"), not(isSuccess("S")));
		assertThat(Try.failure(new RuntimeException()), not(isSuccess("S")));

		assertThat(Try.success("S"), isSuccess("Starts with an S", v -> v.startsWith("S")));
		assertThat(Try.success("B"), not(isSuccess("Starts with an S", v -> v.startsWith("S"))));
		assertThat(Try.failure(new RuntimeException()), not(VavrMatchers.<String> isSuccess("Starts with an S", v -> v.startsWith("S"))));

		assertThat(Try.failure(new RuntimeException()), isFailure());
		assertThat(Try.success("S"), not(isFailure()));

		assertThat(Try.failure(new IllegalStateException()), isFailure(IllegalStateException.class));
		assertThat(Try.failure(new IllegalArgumentException()), not(isFailure(IllegalStateException.class)));
		assertThat(Try.success("S"), not(isFailure(IllegalStateException.class)));

		assertThat(Try.failure(new IllegalArgumentException()), isFailure("Is a runtime exception", t -> t instanceof RuntimeException));
		assertThat(Try.failure(new IOException()), not(isFailure("Is a runtime exception", t -> t instanceof RuntimeException)));
		assertThat(Try.success("Great success!"), not(isFailure("Is a runtime exception", t -> t instanceof RuntimeException)));
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
		assertThat(descriptionOf(isLeft("L", v -> true)), equalTo("is a left Either, matching \"L\""));
		assertThat(descriptionOf(isRight()), equalTo("is a right Either"));
		assertThat(descriptionOf(isRight("R")), equalTo("is a right Either, with value \"R\""));
		assertThat(descriptionOf(isRight("Predicate", v -> true)), equalTo("is a right Either, matching \"Predicate\""));

		assertThat(descriptionOf(isSuccess()), equalTo("is a success"));
		assertThat(descriptionOf(isSuccess("S")), equalTo("is a success, with value \"S\""));
		assertThat(descriptionOf(VavrMatchers.<String> isSuccess("Starts with an S", v -> v.startsWith("S"))), equalTo("is a success, matching \"Starts with an S\""));

		assertThat(descriptionOf(isFailure()), equalTo("is a failure"));
		assertThat(descriptionOf(isFailure(IllegalStateException.class)), equalTo("is a failure, with exception of type \"java.lang.IllegalStateException\""));
		assertThat(descriptionOf(isFailure("Is a runtime exception", t -> t instanceof RuntimeException)), equalTo("is a failure, with throwable matching \"Is a runtime exception\""));

	}

}
