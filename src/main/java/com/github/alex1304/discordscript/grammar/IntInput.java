package com.github.alex1304.discordscript.grammar;

import java.util.function.Predicate;

import reactor.util.annotation.Nullable;

public class IntInput extends InputElement<Integer> {
	
	public IntInput(String description) {
		this(description, null);
	}

	public IntInput(String description, @Nullable Predicate<Integer> valueCond) {
		super(description, Integer::parseInt, valueCond);
	}
}
