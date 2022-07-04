package com.jeroensteenbeeke.vavr.hamcrest;


import io.vavr.concurrent.Future;
import io.vavr.control.Option;
import io.vavr.control.Try;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Future implementation to trigger a rather exotic branch of code
 */
class HackedFuture implements Future<String> {
	@Override
	public Future<String> await() {
		return this;
	}

	@Override
	public Future<String> await(long timeout, TimeUnit unit) {
		return this;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return true;
	}

	@Override
	public ExecutorService executorService() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Option<Try<String>> getValue() {
		return Option.none();
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isCompleted() {
		return true;
	}

	@Override
	public Future<String> onComplete(Consumer<? super Try<String>> action) {
		return this;
	}

	@Override
	public boolean isSuccess() {
		return false;
	}

	@Override
	public boolean isFailure() {
		return true;
	}
}
