package com.github.alex1304.discordscript.language;

import java.util.function.Function;
import java.util.function.Predicate;

import reactor.util.annotation.Nullable;

public class StringInput extends InputElement<String> {
	
	public StringInput() {
		this(null);
	}

	public StringInput(@Nullable Predicate<String> valueCond) {
		super(Function.identity(), valueCond);
	}
}
