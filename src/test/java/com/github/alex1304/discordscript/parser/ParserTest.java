package com.github.alex1304.discordscript.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.alex1304.discordscript.Bot;
import com.github.alex1304.discordscript.language.Grammar;
import com.github.alex1304.discordscript.language.Instruction;
import com.github.alex1304.discordscript.language.Keyword;
import com.github.alex1304.discordscript.language.StringInput;

import reactor.core.publisher.Mono;

public class ParserTest {
	
	private static final Parser PARSER = new Parser(Set.of(() -> new ExampleInstruction1(), () -> new ExampleInstruction2()));

	@Test
	void testParseLine() throws ScriptException {
		var instrList = PARSER.parse("create text channel test\ncreate voice channel test");
		assertTrue(instrList.get(0) instanceof ExampleInstruction1);
		assertTrue(instrList.get(1) instanceof ExampleInstruction2);
	}

}

class ExampleInstruction1 implements Instruction {

	@Override
	public Grammar getGrammar() {
		return Grammar.builder()
				.append(new Keyword("create"))
				.append(new Keyword("text"))
				.append(new Keyword("channel"))
				.append(new StringInput("channel_name"))
				.build();
	}

	@Override
	public Mono<Void> execute(Bot bot) {
		return null;
	}
	
}

class ExampleInstruction2 implements Instruction {

	@Override
	public Grammar getGrammar() {
		return Grammar.builder()
				.append(new Keyword("create"))
				.append(new Keyword("voice"))
				.append(new Keyword("channel"))
				.append(new StringInput("channel_name"))
				.build();
	}

	@Override
	public Mono<Void> execute(Bot bot) {
		return null;
	}
	
}