package com.github.alex1304.discordscript.parser;

public class DefaultState implements ParserState {
	
	private final StringBuilder buffer = new StringBuilder();
	private boolean escaping = false;
	private boolean endOfInstruction = false;
	
	public DefaultState() {
	}

	public DefaultState(boolean endOfInstruction) {
		this.endOfInstruction = endOfInstruction;
	}

	@Override
	public void read(Parser parser, int c) {
		if (c == '\n') {
			parser.incrementLineNumber();
		}
		if (escaping) {
			buffer.appendCodePoint(c);
			escaping = false;
			return;
		}
		if (c == '|') {
			endOfInstruction = false;
			return;
		}
		if (endOfInstruction) {
			parser.nextInstruction();
			endOfInstruction = false;
		}
		if (Character.isWhitespace(c)) {
			if (c == '\n') {
				endOfInstruction = true;
			}
			if (buffer.length() > 0) {
				parser.addToken(buffer.toString());
				buffer.setLength(0);
				buffer.trimToSize();
			}
			return;
		}
		if (c == '\\') {
			escaping = true;
			return;
		}
		if (c == '#') {
			parser.setState(new CommentState());
			return;
		}
		if (c == '"') {
			parser.setState(new StringState());
			return;
		}
		buffer.appendCodePoint(c);
	}

	@Override
	public void complete(Parser parser) {
		parser.addToken(buffer.toString());
	}
}
