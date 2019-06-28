package com.github.alex1304.discordscript.language;

import com.github.alex1304.discordscript.Bot;

import reactor.core.publisher.Mono;

/**
 * Represents an instruction in the Discord Script language.
 */
public interface Instruction {
	/**
	 * Provides the grammar for this instruction. It is important that this method
	 * returns a NEW instance of the Grammar object on each call
	 * 
	 * @return the grammar
	 */
	Grammar getGrammar();

	/**
	 * Executes the instruction.
	 * 
	 * @param bot the bot instance
	 * @return a Mono completing when the instruction is complete
	 */
	Mono<Void> execute(Bot bot);
}
