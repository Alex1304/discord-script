package com.github.alex1304.discordscript.language;

import java.awt.Color;
import java.util.Optional;

import com.github.alex1304.discordscript.grammar.ColorInput;
import com.github.alex1304.discordscript.grammar.Grammar;
import com.github.alex1304.discordscript.grammar.Keyword;
import com.github.alex1304.discordscript.grammar.StringInput;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class CreateRoleInstruction implements Instruction {
	
	private String name = "";
	private Optional<Color> color = Optional.empty();
	
	@Override
	public Grammar getGrammar() {
		return Grammar.builder()
				.add(new Keyword("create"))
				.add(new Keyword("role"))
				.add(new StringInput("role_name"), v -> name = v)
				.addOptions(
						Grammar.builder()
								.add(new Keyword("color"))
								.add(new ColorInput("color hex code"), v -> color = Optional.of(v))
								.build())
				.build();
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		return event.getGuild()
				.flatMap(guild -> guild.createRole(spec -> {
					spec.setName(name);
					color.ifPresent(spec::setColor);
				}))
				.then();
	}
}
