package com.github.alex1304.discordscript.language;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import reactor.util.annotation.Nullable;

/**
 * Automaton-based grammar resolver.
 */
public class Grammar {
	
	private final State<?> initialState;
	private State<?> currentState;
	private State<?> loopBack;
	private boolean isComplete;
	
	private Grammar(State<?> initialState) {
		this.initialState = initialState;
		this.currentState = initialState;
		this.loopBack = null;
		this.isComplete = false;
	}
	
	public boolean consumeNext(String token) {
		if (isComplete) {
			throw new IllegalStateException("Grammar already complete");
		}
		if (currentState != null && currentState.consume(token)) {
			loopBack = currentState.loopBack;
			currentState = currentState.next;
			return true;
		}
		if (loopBack != null && loopBack.consume(token)) {
			currentState = loopBack.next;
			loopBack = loopBack.loopBack;
			return true;
		}
		isComplete = true;
		return false;
	}
	
	/**
	 * Marks this grammar as complete.
	 * 
	 * @return true if it was on a terminal state, false otherwise
	 */
	public boolean complete() {
		if (isComplete) {
			throw new IllegalStateException("Grammar already complete");
		}
		isComplete = true;
		return isOnTerminalState();
	}
	
	/**
	 * Returns a new instance of this grammar that is reset to its initial state.
	 * 
	 * @return a new grammar identical to this one but reset to initial state.
	 */
	public Grammar freshInstance() {
		return new Grammar(initialState);
	}
	
	/**
	 * Provides a list containing the descriptions of the next expected tokens.
	 * 
	 * @return a String list, one for each expected token
	 */
	public List<String> describeExpectedTokens() {
		var expecteds = new ArrayList<String>();
		if (currentState != null) {
			expecteds.add(currentState.element.describeExpectedValue());
		}
		if (loopBack != null) {
			expecteds.add(loopBack.element.describeExpectedValue());
		}
		return expecteds;
	}
	
	/**
	 * Checks if the current state of the grammar is terminal. It differs from
	 * {@link #complete()} in that it doesn't mark this grammar as complete after
	 * this is called.
	 * 
	 * @return true if the grammar is on a terminal state, false otherwise
	 */
	public boolean isOnTerminalState() {
		return currentState == null || currentState.isTerminal;
	}
	
	private static class State<T> {
		private final GrammarElement<T> element;
		private Consumer<T> onResolve;
		private State<?> next;
		private State<?> loopBack;
		private boolean isTerminal;
		State(GrammarElement<T> element, Consumer<T> onResolve, boolean isTerminal) {
			this.element = element;
			this.onResolve = onResolve != null ? onResolve : x -> {};
			this.isTerminal = isTerminal;
		}
		boolean consume(String token) {
			return element.value(token).map(val -> {
				onResolve.accept(val);
				return true;
			}).orElse(false);
		}
	}
	
	public static class GrammarBuilder {
		private GrammarBuilder() {
		}
		
		private State<?> head;
		private State<?> tail;
		private boolean optional;
		
		public <T> GrammarBuilder append(GrammarElement<T> element, @Nullable Consumer<T> onResolve) {
			var state = new State<>(element, onResolve, optional);
			if (head == null) {
				head = state;
				tail = state;
			} else {
				tail.next = state;
				tail = state;
			}
			return this;
		}
		
		public <T> GrammarBuilder append(GrammarElement<T> element) {
			return append(element, null);
		}
		
		public GrammarBuilder optional() {
			optional = true;
			return this;
		}
		
		public <T> GrammarBuilder appendSubgrammar(GrammarBuilder subgrammar, boolean repeatable) {
			if (repeatable) {
				subgrammar.tail.loopBack = subgrammar.head;
			}
			tail.next = subgrammar.head;
			tail.next.isTerminal = optional;
			tail = subgrammar.tail;
			return this;
		}
		
		public Grammar build() {
			return new Grammar(head);
		}
	}
	
	public static GrammarBuilder builder() {
		return new GrammarBuilder();
	}
}
