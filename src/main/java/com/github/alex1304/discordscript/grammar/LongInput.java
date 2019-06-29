package com.github.alex1304.discordscript.grammar;

import java.util.function.Predicate;

import reactor.util.annotation.Nullable;

public class LongInput extends InputElement<Long> {
	
	public LongInput(String description) {
		this(description, null);
	}

	public LongInput(String description, @Nullable Predicate<Long> valueCond) {
		super(description, Long::parseLong, valueCond);
	}
}
