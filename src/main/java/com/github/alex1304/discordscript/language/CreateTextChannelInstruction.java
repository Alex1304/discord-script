package com.github.alex1304.discordscript.language;

import java.util.Optional;

import com.github.alex1304.discordscript.grammar.Grammar;
import com.github.alex1304.discordscript.grammar.IntInput;
import com.github.alex1304.discordscript.grammar.Keyword;
import com.github.alex1304.discordscript.grammar.StringInput;
import com.github.alex1304.discordscript.util.DiscordUtils;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Category;
import reactor.core.publisher.Mono;

public class CreateTextChannelInstruction implements Instruction {
	
	private String name = "";
	private boolean nsfw = false;
	private Optional<String> category = Optional.empty();
	private Optional<String> topic = Optional.empty();
	private Optional<Integer> position = Optional.empty();
	private Optional<Integer> slowmode = Optional.empty();
	
	@Override
	public Grammar getGrammar() {
		return Grammar.builder()
				.add(new Keyword("create"))
				.add(new Keyword("text"))
				.add(new Keyword("channel"))
				.add(new StringInput("channel name"), v -> name = v)
				.addOptions(
						Grammar.builder()
								.add(new Keyword("position"))
								.add(new IntInput("position number"), v -> position = Optional.of(v))
								.build(),
						Grammar.builder()
								.add(new Keyword("category"))
								.add(new StringInput("category name"), v -> category = Optional.of(v))
								.build(),
						Grammar.builder()
								.add(new Keyword("topic"))
								.add(new StringInput("channel topic"), v -> topic = Optional.of(v))
								.build(),
						Grammar.of(new Keyword("nsfw"), v -> nsfw = true),
						Grammar.builder()
								.add(new Keyword("slowmode"))
								.add(new IntInput("seconds"), v -> slowmode = Optional.of(v))
								.build())
				.build();
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		return Mono.justOrEmpty(category)
				.flatMap(ctgName -> DiscordUtils.getCategoryByName(event, ctgName)
						.map(Category::getId)
						.map(Optional::of)
						.switchIfEmpty(Mono.error(new RuntimeException("No category matches the name `" + ctgName + "`"))))
				.defaultIfEmpty(Optional.empty())
				.flatMap(categoryId -> event.getGuild()
						.flatMap(guild -> guild.createTextChannel(spec -> {
							spec.setName(name);
							spec.setNsfw(nsfw);
							categoryId.ifPresent(spec::setParentId);
							topic.ifPresent(spec::setTopic);
							position.ifPresent(spec::setPosition);
							slowmode.ifPresent(spec::setRateLimitPerUser);
						})))
				.then();
	}

}
