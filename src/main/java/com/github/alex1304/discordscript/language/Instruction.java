package com.github.alex1304.discordscript.language;

import com.github.alex1304.discordscript.Bot;

import reactor.core.publisher.Mono;

/**
 * Represents an instruction in the Discord Script language.
 */
public interface Instruction {

	/**
	 * Executes the instruction.
	 * 
	 * @param bot the bot instance
	 * @return a Mono completing when the instruction is complete
	 */
	Mono<Void> execute(Bot bot);
}
