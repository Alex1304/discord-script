package com.github.alex1304.discordscript.parser;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.github.alex1304.discordscript.grammar.Grammar;
import com.github.alex1304.discordscript.grammar.Keyword;
import com.github.alex1304.discordscript.grammar.StringInput;
import com.github.alex1304.discordscript.language.Instruction;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class ParserTest {
	
	private static final Parser PARSER = new Parser(Set.of(() -> new ExampleInstruction1(), () -> new ExampleInstruction2()));

	@Test
	void testParseLine() throws ScriptException {
		var instrList = PARSER.parse("create text channel test\ncreate voice channel test");
		assertTrue(instrList.get(0).getT2() instanceof ExampleInstruction1);
		assertTrue(instrList.get(1).getT2() instanceof ExampleInstruction2);
	}

}

class ExampleInstruction1 implements Instruction {

	@Override
	public Grammar getGrammar() {
		return Grammar.builder()
				.add(new Keyword("create"))
				.add(new Keyword("text"))
				.add(new Keyword("channel"))
				.add(new StringInput("channel_name"))
				.build();
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		return null;
	}
	
}

class ExampleInstruction2 implements Instruction {

	@Override
	public Grammar getGrammar() {
		return Grammar.builder()
				.add(new Keyword("create"))
				.add(new Keyword("voice"))
				.add(new Keyword("channel"))
				.add(new StringInput("channel_name"))
				.build();
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		return null;
	}
	
}