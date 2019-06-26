package com.github.alex1304.discordscript.language;

import com.github.alex1304.discordscript.Bot;

import reactor.core.publisher.Mono;

public interface Instruction {

	Mono<Void> execute(Bot bot);
}
