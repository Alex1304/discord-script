package com.github.alex1304.discordscript.parser;

import com.github.alex1304.discordscript.parser.Parser.ParseProcess;

/**
 * State indicating that the parser is parsing a comment.
 */
public class CommentState implements ParserState {

	@Override
	public void read(ParseProcess parser, int c) {
		if (c == '\n') {
			parser.setState(new DefaultState(true));
		}
	}
}
