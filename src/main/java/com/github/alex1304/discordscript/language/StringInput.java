package com.github.alex1304.discordscript.language;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import reactor.util.annotation.Nullable;

public class StringInput extends InputElement<String> {
	
	private final String description;
	
	public StringInput(String description) {
		this(description, null);
	}

	public StringInput(String description, @Nullable Predicate<String> valueCond) {
		super(Function.identity(), valueCond);
		this.description = Objects.requireNonNull(description);
	}
	
	@Override
	String describeExpectedValue0() {
		return description;
	}
}
