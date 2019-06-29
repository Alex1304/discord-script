package com.github.alex1304.discordscript.language;

import com.github.alex1304.discordscript.grammar.Grammar;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

/**
 * Represents an instruction in the Discord Script language.
 */
public interface Instruction {
	/**
	 * Provides the grammar for this instruction.
	 * 
	 * @return the grammar
	 */
	Grammar getGrammar();

	/**
	 * Executes the instruction.
	 * 
	 * @param event the MessageCreateEvent of the message declaring the instruction
	 * @return a Mono completing when the instruction is complete
	 */
	Mono<Void> execute(MessageCreateEvent event);
}
