package com.github.alex1304.discordscript;

import discord4j.core.DiscordClient;
import reactor.core.publisher.Mono;

/**
 * Represents the bot itself.
 */
public class Bot {
	//private static final Logger LOGGER = LoggerFactory.getLogger(Bot.class);
	
	private final String token;
	private final DiscordClient discordClient;

	public Bot(String token, DiscordClient discordClient) {
		this.token = token;
		this.discordClient = discordClient;
	}

	/**
	 * Get the bot token.
	 * 
	 * @return the token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Gets the discord client.
	 * 
	 * @return the discord client
	 */
	public DiscordClient getDiscordClient() {
		return discordClient;
	}

	public Mono<Void> start() {
		return discordClient.login();
	}
}
