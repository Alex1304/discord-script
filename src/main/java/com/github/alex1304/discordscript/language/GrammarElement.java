package com.github.alex1304.discordscript.language;

import java.util.Optional;

/**
 * Represents a grammar element in the Discord Script language.
 * 
 * @param <T> the type of value represented by this element
 */
public interface GrammarElement<T> {
	/**
	 * Checks if the given input matches this element, and returns the value if so.
	 * Otherwise, an epty Optional is returned.
	 * 
	 * @param input the input
	 * @return an Optional with the recognized value, or empty if not recognized
	 */
	Optional<T> value(String input);
	
	/**
	 * Provides a description for the expected value, used for documentation purposes.
	 * 
	 * @return a String describing the expected format of value
	 */
	String describeExpectedValue();
}
