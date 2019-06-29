package com.github.alex1304.discordscript.util;

import java.io.IOException;
import java.time.Duration;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Category;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.VoiceChannel;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.retry.Retry;

public class DiscordUtils {

	private DiscordUtils() {
	}
	
	public static Mono<Category> getCategoryByName(MessageCreateEvent event, String name) {
		return event.getGuild()
				.flatMapMany(Guild::getChannels)
				.ofType(Category.class)
				.filter(ctg -> ctg.getName().equalsIgnoreCase(name))
				.next();
	}
	
	public static Mono<TextChannel> getTextChannelByName(MessageCreateEvent event, String name) {
		return event.getGuild()
				.flatMapMany(Guild::getChannels)
				.ofType(TextChannel.class)
				.filter(c -> c.getName().equalsIgnoreCase(name))
				.next();
	}
	
	public static Mono<VoiceChannel> getVoiceChannelByName(MessageCreateEvent event, String name) {
		return event.getGuild()
				.flatMapMany(Guild::getChannels)
				.ofType(VoiceChannel.class)
				.filter(c -> c.getName().equalsIgnoreCase(name))
				.next();
	}
	
	public static Mono<Role> getRoleByName(MessageCreateEvent event, String name) {
		return event.getGuild()
				.flatMapMany(Guild::getRoles)
				.filter(ctg -> ctg.getName().equalsIgnoreCase(name))
				.next();
	}
	
	public static Mono<String> getFileContent(HttpClient fileClient, Attachment attachment) {
		return fileClient.get()
				.uri(attachment.getUrl())
				.responseSingle((response, content) -> {
					if (response.status().code() / 100 != 2) {
						return Mono.error(new RuntimeException("Received " + response.status().code() + " "
								+ response.status().reasonPhrase() + " from Discord CDN"));
					}
					return content.asString();
				})
				.retryWhen(Retry.anyOf(IOException.class)
						.exponentialBackoffWithJitter(Duration.ofSeconds(1), Duration.ofMinutes(1)))
				.timeout(Duration.ofMinutes(2), Mono.error(new RuntimeException("CDN took too long to respond. Try again later.")));
	}
}
