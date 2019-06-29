package com.github.alex1304.discordscript;

import java.time.Duration;
import java.util.Set;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.alex1304.discordscript.language.*;
import com.github.alex1304.discordscript.parser.Parser;
import com.github.alex1304.discordscript.parser.ScriptException;
import com.github.alex1304.discordscript.util.DiscordUtils;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.netty.http.client.HttpClient;
import reactor.retry.Retry;

/**
 * Represents the bot itself.
 */
public class Bot {
	private static final Logger LOGGER = LoggerFactory.getLogger(Bot.class);
	
	private final DiscordClient discordClient;
	private final Parser parser;
	private final HttpClient fileClient;

	public Bot(DiscordClient discordClient) {
		this.discordClient = discordClient;
		this.parser = initParser();
		this.fileClient = HttpClient.create().headers(h -> h.add("Content-Type", "text/plain"));
	}

	/**
	 * Gets the discord client.
	 * 
	 * @return the discord client
	 */
	public DiscordClient getDiscordClient() {
		return discordClient;
	}

	/**
	 * Starts the bot.
	 * 
	 * @return a Mono that should be subscribed to in order to start the bot.
	 */
	public Mono<Void> start() {
		discordClient.getEventDispatcher().on(MessageCreateEvent.class)
				.filterWhen(event -> event.getGuild()
						.flatMap(g -> Mono.justOrEmpty(event.getMessage().getAuthor())
								.map(User::getId)
								.map(g.getOwnerId()::equals)))
				.flatMap(this::handleEvent)
				.retryWhen(Retry.any()
						.doOnRetry(ctx -> LOGGER.error("Error when processing MessageCreateEvent", ctx.exception())))
				.repeat()
				.subscribe();
		return discordClient.login();
	}
	
	private Mono<Void> handleEvent(MessageCreateEvent event) {
		return Mono.zip(Mono.justOrEmpty(discordClient.getSelfId().map(Snowflake::asString)), Mono.justOrEmpty(event.getMessage().getContent()))
				.flatMap(TupleUtils.function((selfId, content) -> Flux.just("<@" + selfId + ">", "<@!" + selfId + ">")
						.filter(content::startsWith)
						.next()
						.map(prefix -> content.substring(prefix.length()))))
				.filter(code -> !code.isBlank())
				.switchIfEmpty(Flux.fromIterable(event.getMessage().getAttachments())
						.filter(att -> att.getFilename().toLowerCase().endsWith(".ds"))
						.next()
						.flatMap(att -> DiscordUtils.getFileContent(fileClient, att))
						.onErrorResume(e -> reply(event, ":no_entry_sign: Failed to download file: " + e.getMessage())))
				.flatMap(code -> executeScript(event, code))
				.onErrorResume(ScriptExecutionException.class, e -> reply(event, e.getMessage()))
				.then();
	}
	
	private <T> Mono<T> reply(MessageCreateEvent event, String message) {
		return event.getMessage().getChannel().flatMap(c -> c.createMessage(message)).then(Mono.empty());
	}
	
	private Mono<Void> executeScript(MessageCreateEvent event, String code) {
		try {
			var instructions = parser.parse(code);
			return Flux.fromIterable(instructions)
					.concatMap(TupleUtils.function((source, instr) -> instr.execute(event)
							.then(Mono.delay(Duration.ofMillis(200))) // Avoid race conditions
							.log("bot.executeScript", Level.FINE)
							.onErrorMap(e -> new ScriptExecutionException(source, e))))
					.then(reply(event, ":white_check_mark: Script execution complete!"));
		} catch (ScriptException e) {
			return reply(event, ":no_entry_sign: **The source code contains syntax errors**\n" + e.getMessage());
		}
	}
	
	private Parser initParser() {
		return new Parser(Set.of(
				() -> new CreateTextChannelInstruction(),
				() -> new CreateVoiceChannelInstruction(),
				() -> new CreateCategoryInstruction(),
				() -> new CreateRoleInstruction(),
				() -> new ConfigurePermissionsInstruction(),
				() -> new MoveRoleInstruction()
		));
	}
}
