package com.github.alex1304.discordscript.language;

import com.github.alex1304.discordscript.grammar.Grammar;
import com.github.alex1304.discordscript.grammar.IntInput;
import com.github.alex1304.discordscript.grammar.Keyword;
import com.github.alex1304.discordscript.grammar.StringInput;
import com.github.alex1304.discordscript.util.DiscordUtils;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public class MoveRoleInstruction implements Instruction {
	
	private String name;
	private int position;
	
	@Override
	public Grammar getGrammar() {
		return Grammar.builder()
				.add(new Keyword("move"))
				.add(new Keyword("role"))
				.add(new StringInput("role name"), v -> name = v)
				.add(new Keyword("position"))
				.add(new IntInput("position number"), v -> position = v)
				.build();
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		return DiscordUtils.getRoleByName(event, name)
				.flatMapMany(role -> role.changePosition(position))
				.then();
	}

}
