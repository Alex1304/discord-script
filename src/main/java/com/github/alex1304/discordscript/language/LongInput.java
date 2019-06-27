package com.github.alex1304.discordscript.language;

import java.util.function.Predicate;

import reactor.util.annotation.Nullable;

public class LongInput extends InputElement<Long> {
	
	public LongInput() {
		this(null);
	}

	public LongInput(@Nullable Predicate<Long> valueCond) {
		super(Long::parseLong, valueCond);
	}
}
