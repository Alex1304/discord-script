package com.github.alex1304.discordscript.parser;

public class StringState implements ParserState {
	
	private final StringBuilder buffer = new StringBuilder();
	private boolean escaping;
	
	@Override
	public void read(Parser parser, int c) {
		if (c == '\n') {
			parser.incrementLineNumber();
		}
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
}
