package com.github.alex1304.discordscript.language;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import reactor.util.annotation.Nullable;

abstract class InputElement<T> implements GrammarElement<T> {

	private final Predicate<T> valueCond;
	private final Function<String, T> parser;
	
	InputElement(Function<String, T> parser) {
		this(parser, null);
	}
	
	InputElement(Function<String, T> parser, @Nullable Predicate<T> valueCond) {
		this.valueCond = valueCond == null ? x -> true : valueCond;
		this.parser = parser;
	}

	@Override
	public Optional<T> value(String input) {
		return Optional.of(input).map(parser).filter(valueCond);
	}
}
