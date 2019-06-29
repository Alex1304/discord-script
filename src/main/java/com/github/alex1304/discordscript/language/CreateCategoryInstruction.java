package com.github.alex1304.discordscript.language;

import java.util.Optional;

import com.github.alex1304.discordscript.grammar.Grammar;
import com.github.alex1304.discordscript.grammar.IntInput;
import com.github.alex1304.discordscript.grammar.Keyword;
import com.github.alex1304.discordscript.grammar.StringInput;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class CreateCategoryInstruction implements Instruction {
	
	private String name = "";
	private Optional<Integer> position = Optional.empty();
	
	@Override
	public Grammar getGrammar() {
		return Grammar.builder()
				.add(new Keyword("create"))
				.add(new Keyword("category"))
				.add(new StringInput("category name"), v -> name = v)
				.addOptions(
						Grammar.builder()
								.add(new Keyword("position"))
								.add(new IntInput("position number"), v -> position = Optional.of(v))
								.build())
				.build();
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		return event.getGuild()
				.flatMap(guild -> guild.createCategory(spec -> {
					spec.setName(name);
					position.ifPresent(spec::setPosition);
				}))
				.then();
	}

}
