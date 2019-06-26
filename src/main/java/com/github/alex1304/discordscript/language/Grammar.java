package com.github.alex1304.discordscript.language;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class Grammar {
	
	private State<?> currentState;
	private State<?> loopBack;
	
	private Grammar(State<?> initialState) {
		this.currentState = initialState;
		this.loopBack = null;
	}
	
	public boolean next(String rawToken) {
		if (currentState == null) {
			return false;
		}
		if (currentState.token.matches(rawToken) && currentState.checkAndResolve()) {
			loopBack = currentState.loopBack;
			currentState = currentState.next;
			return true;
		}
		if (loopBack != null && loopBack.token.matches(rawToken) && loopBack.checkAndResolve()) {
			loopBack = loopBack.loopBack;
			currentState = loopBack.next;
			return true;
		}
		return false;
	}
	
	public boolean complete() {
		return currentState == null || currentState.isTerminal;
	}
	
	private static class State<T> {
		private final Token<T> token;
		private final Predicate<T> cond;
		private Consumer<T> onResolve;
		private State<?> next;
		private State<?> loopBack;
		private boolean isTerminal;
		State(Token<T> token, Predicate<T> cond, Consumer<T> onResolve, boolean isTerminal) {
			this.token = token;
			this.cond = cond;
			this.onResolve = onResolve;
			this.isTerminal = isTerminal;
		}
		boolean checkAndResolve() {
			T val = token.value();
			if (cond.test(val)) {
				onResolve.accept(val);
				return true;
			}
			return false;
		}
		void addActionOnResolve(Runnable action) {
			onResolve = onResolve.andThen(__ -> action.run());
		}
	}
	
	public static class GrammarBuilder {
		private GrammarBuilder() {
		}
		
		private State<?> head;
		private State<?> tail;
		private boolean optional;
		
		public <T> void append(Token<T> token, Consumer<T> onResolve) {
			append(token, (Predicate<T>) x -> true, onResolve);
		}
		
		public <T> void append(Token<T> token, T expectedValue, Consumer<T> onResolve) {
			append(token, (Predicate<T>) x -> x.equals(expectedValue), onResolve);
		}
		
		public <T> void append(Token<T> token, Predicate<T> valueCondition, Consumer<T> onResolve) {
			var state = new State<>(token, valueCondition, onResolve, optional);
			if (head == null) {
				head = state;
				tail = state;
			} else {
				tail.next = state;
				tail = state;
			}
		}
		
		public void optional() {
			optional = true;
		}
		
		public <T> void appendSubgrammar(GrammarBuilder subgrammar, boolean repeatable, Runnable onResolve) {
			tail.next = subgrammar.head;
			if (repeatable) {
				subgrammar.tail.loopBack = tail;
			}
			tail = subgrammar.tail;
			tail.addActionOnResolve(onResolve);
		}
	}
	
	public GrammarBuilder builder() {
		return new GrammarBuilder();
	}
}
