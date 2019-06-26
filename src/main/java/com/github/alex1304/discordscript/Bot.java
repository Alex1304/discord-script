package com.github.alex1304.discordscript;

import discord4j.core.DiscordClient;
import reactor.core.publisher.Mono;

/**
 * Represents the bot itself.
 */
public class Bot {
	//private static final Logger LOGGER = LoggerFactory.getLogger(Bot.class);
	
	private final DiscordClient discordClient;

	public Bot(DiscordClient discordClient) {
		this.discordClient = discordClient;
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
		return discordClient.login();
	}
}
