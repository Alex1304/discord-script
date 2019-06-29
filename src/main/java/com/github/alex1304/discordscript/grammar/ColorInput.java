package com.github.alex1304.discordscript.grammar;

import java.awt.Color;
import java.util.function.Predicate;

import reactor.util.annotation.Nullable;

public class ColorInput extends InputElement<Color> {
	
	public ColorInput(String description) {
		this(description, null);
	}

	public ColorInput(String description, @Nullable Predicate<Color> valueCond) {
		super(description, Color::decode, valueCond);
	}
}
