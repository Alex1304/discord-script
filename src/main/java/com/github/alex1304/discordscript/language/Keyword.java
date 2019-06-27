package com.github.alex1304.discordscript.language;

import java.util.Arrays;

public class Keyword extends StringInput {
	
	public Keyword(String... keywords) {
		super(v -> Arrays.stream(keywords).anyMatch(k -> v.equalsIgnoreCase(k)));
	}
}
