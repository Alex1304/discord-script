package com.github.alex1304.discordscript.language;

/**
 * Represents a token in the Discord Script language.
 * 
 * @param <T> the type of value represented by this token
 */
public interface Token<T> {
	
	/**
	 * Checks if the given input matches the token.
	 * 
	 * @param input the input
	 * @return true if it matches this token, false otherwise
	 */
	boolean matches(String input);
	
	/**
	 * Gets the value of the token.
	 * 
	 * @return the value
	 */
	T value();
}
