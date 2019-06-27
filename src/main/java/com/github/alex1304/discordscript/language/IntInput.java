package com.github.alex1304.discordscript.language;

import java.util.function.Predicate;

import reactor.util.annotation.Nullable;

public class IntInput extends InputElement<Integer> {
	
	public IntInput() {
		this(null);
	}

	public IntInput(@Nullable Predicate<Integer> valueCond) {
		super(Integer::parseInt, valueCond);
	}
}
