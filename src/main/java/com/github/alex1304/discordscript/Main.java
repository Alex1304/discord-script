package com.github.alex1304.discordscript;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.object.data.stored.MessageBean;
import discord4j.core.object.data.stored.PresenceBean;
import discord4j.core.object.data.stored.VoiceStateBean;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.store.api.mapping.MappingStoreService;
import discord4j.store.api.noop.NoOpStoreService;
import discord4j.store.jdk.JdkStoreService;
import reactor.core.scheduler.Schedulers;

public class Main {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		if (args.length == 0) {
			LOGGER.error("Bot token not provided");
			System.exit(1);
		}
		var token = args[0];
		var discordClient = new DiscordClientBuilder(token)
				.setStoreService(MappingStoreService.create()
						.setMappings(new NoOpStoreService(), MessageBean.class, VoiceStateBean.class, PresenceBean.class)
						.setFallback(new JdkStoreService()))
				.setEventScheduler(Schedulers.immediate())
				.setInitialPresence(Presence.online(Activity.playing("Discord Hack Week 2019")))
				.build();
		var bot = new Bot(token, discordClient);
		bot.start().block();
	}

}
