package com.github.alex1304.discordscript.language;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class Keyword implements GrammarElement<String> {
	
	private final String[] keywords;
	
	public Keyword(String... keywords) {
		this.keywords = keywords;
	}

	@Override
	public Optional<String> value(String input) {
		return Optional.of(input)
				.filter(v -> Arrays.stream(Objects.requireNonNull(keywords))
						.anyMatch(k -> v.equalsIgnoreCase(k)));
	}
	
	@Override
	public String describeExpectedValue() {
		return String.join("|", keywords);
	}
}
