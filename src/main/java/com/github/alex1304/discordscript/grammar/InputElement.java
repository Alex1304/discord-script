package com.github.alex1304.discordscript.grammar;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import reactor.util.annotation.Nullable;

abstract class InputElement<T> implements GrammarElement<T> {

	private final String description;
	private final Predicate<T> valueCond;
	private final Function<String, T> parser;
	
	InputElement(String description, Function<String, T> parser) {
		this(description, parser, null);
	}
	
	InputElement(String description, Function<String, T> parser, @Nullable Predicate<T> valueCond) {
		this.description = Objects.requireNonNull(description);
		this.parser = Objects.requireNonNull(parser);
		this.valueCond = valueCond == null ? x -> true : valueCond;
	}

	@Override
	public Optional<T> value(String input) {
		try {
			return Optional.of(input).map(parser).filter(valueCond);
		} catch (RuntimeException e) {
			return Optional.empty();
		}
	}
	
	@Override
	public String describeExpectedValue() {
		return "<" + description + ">";
	}
}
