package com.github.alex1304.discordscript.parser;

/**
 * Represents a state of the parser. Each state has its own way to interpret the
 * input characters.
 */
public interface ParserState {

	/**
	 * Reads the character.
	 * 
	 * @param parser the parser
	 * @param c      the character to read
	 */
	void read(Parser parser, int c);
	
	/**
	 * Action when the parsing is complete. Nothing by default.
	 */
	default void complete(Parser parser) {
		return;
	}
}
