package com.github.alex1304.discordscript.parser;

import com.github.alex1304.discordscript.parser.Parser.ParseProcess;

/**
 * State indicating that the parser is parsing a string value.
 */
public class StringState implements ParserState {
	
	private final StringBuilder buffer = new StringBuilder();
	private boolean escaping;
	
	@Override
	public void read(ParseProcess parser, int c) {
		if (escaping) {
			buffer.appendCodePoint(c);
			escaping = false;
		}
		if (c == '"') {
			parser.addToken(buffer.toString());
			parser.setState(new DefaultState());
			return;
		}
		buffer.appendCodePoint(c);
	}

	@Override
	public void complete(ParseProcess parser) {
		if (buffer.length() > 0) {
			parser.addToken(buffer.toString());
		}
	}
}
