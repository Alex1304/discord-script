package com.github.alex1304.discordscript.parser;

public class CommentState implements ParserState {

	@Override
	public void read(Parser parser, int c) {
		if (c == '\n') {
			parser.incrementLineNumber();
			parser.setState(new DefaultState(true));
		}
	}
}
