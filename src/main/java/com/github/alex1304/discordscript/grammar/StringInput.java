package com.github.alex1304.discordscript.grammar;

import java.util.function.Function;
import java.util.function.Predicate;

import reactor.util.annotation.Nullable;

public class StringInput extends InputElement<String> {
	
	public StringInput(String description) {
		this(description, null);
	}

	public StringInput(String description, @Nullable Predicate<String> valueCond) {
		super(description, Function.identity(), valueCond);
	}
}
