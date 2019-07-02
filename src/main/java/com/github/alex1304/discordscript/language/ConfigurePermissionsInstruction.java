package com.github.alex1304.discordscript.language;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.github.alex1304.discordscript.grammar.Grammar;
import com.github.alex1304.discordscript.grammar.Keyword;
import com.github.alex1304.discordscript.grammar.StringInput;
import com.github.alex1304.discordscript.util.DiscordUtils;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.util.Permission;
import discord4j.core.object.util.PermissionSet;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ConfigurePermissionsInstruction implements Instruction {
	
	private static final String[] PERMS = Arrays.stream(Permission.values())
												.map(Permission::name)
												.toArray(String[]::new);
	
	private String role;
	private final Set<Permission> defaultPerms = new HashSet<>();
	private final Map<String, Set<Permission>> allowedOverwrites = new HashMap<>();
	private final Map<String, Set<Permission>> deniedOverwrites = new HashMap<>();
	
	@Override
	public Grammar getGrammar() {
		var channelNameTmp = new AtomicReference<String>();
		return Grammar.builder()
				.add(new Keyword("configure"))
				.add(new Keyword("permissions", "perms"))
				.add(new Keyword("for"))
				.add(new StringInput("role name"), v -> role = v)
				.addOptions(
						Grammar.builder()
								.add(new Keyword("on"))
								.add(new StringInput("channel name"), channelNameTmp::set)
								.add(new Keyword("allow"))
								.add(new Keyword(PERMS), v -> allowedOverwrites
										.computeIfAbsent(channelNameTmp.get().toLowerCase(), __ -> new HashSet<>())
										.add(Permission.valueOf(v.toUpperCase())))
								.addOptions(Grammar.of(new Keyword(PERMS), v -> allowedOverwrites
										.computeIfAbsent(channelNameTmp.get().toLowerCase(), __ -> new HashSet<>())
										.add(Permission.valueOf(v.toUpperCase()))))
								.build(),
						Grammar.builder()
								.add(new Keyword("on"))
								.add(new StringInput("channel name"), channelNameTmp::set)
								.add(new Keyword("deny"))
								.add(new Keyword(PERMS), v -> deniedOverwrites
										.computeIfAbsent(channelNameTmp.get().toLowerCase(), __ -> new HashSet<>())
										.add(Permission.valueOf(v.toUpperCase())))
								.addOptions(Grammar.of(new Keyword(PERMS), v -> deniedOverwrites
										.computeIfAbsent(channelNameTmp.get().toLowerCase(), __ -> new HashSet<>())
										.add(Permission.valueOf(v.toUpperCase()))))
								.build())
				.add(new Keyword("default"))
				.addOptions(Grammar.of(new Keyword(PERMS), v -> defaultPerms.add(Permission.valueOf(v.toUpperCase()))))
				.build();
	}

	@Override
	public Mono<Void> execute(MessageCreateEvent event) {
		var roleId = new AtomicReference<Snowflake>();
		return DiscordUtils.getRoleByName(event, role)
				.switchIfEmpty(Mono.error(new RuntimeException("No role found with name '" + role + "'.")))
				.doOnNext(role -> roleId.set(role.getId()))
				.flatMap(role -> role.edit(spec -> spec.setPermissions(PermissionSet.of(defaultPerms.toArray(Permission[]::new)))))
				.and(Flux.defer(() -> Flux.merge(Flux.fromIterable(allowedOverwrites.keySet()), Flux.fromIterable(deniedOverwrites.keySet()))
						.distinct()
						.flatMap(name -> DiscordUtils.getTextChannelByName(event, name)
								.cast(GuildChannel.class)
								.switchIfEmpty(DiscordUtils.getVoiceChannelByName(event, name))
								.switchIfEmpty(Mono.error(new RuntimeException("No channel found with name '" + name + "'"))))
						.flatMap(channel -> channel.addRoleOverwrite(roleId.get(), 
								PermissionOverwrite.forRole(roleId.get(),
										PermissionSet.of(Optional.ofNullable(allowedOverwrites.get(channel.getName().toLowerCase()))
												.map(set -> set.toArray(Permission[]::new))
												.orElse(new Permission[0])),
										PermissionSet.of(Optional.ofNullable(deniedOverwrites.get(channel.getName().toLowerCase()))
												.map(set -> set.toArray(Permission[]::new))
												.orElse(new Permission[0])))))))
				.then();
	}

}
