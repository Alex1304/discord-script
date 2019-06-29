package com.github.alex1304.discordscript.grammar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import reactor.util.annotation.Nullable;

/**
 * Automaton-based grammar resolver.
 */
public class Grammar {

	private final State<?> initialState;
	private State<?> state;
	private final List<Grammar> optionsOfPreviousState;
	private Set<Grammar> processedOptions;
	private boolean isComplete;
	private final List<String> consumedTokens;
	
	private Grammar(State<?> initialState) {
		this.initialState = initialState;
		this.state = initialState;
		this.optionsOfPreviousState = new ArrayList<>();
		this.processedOptions = new HashSet<>();
		this.isComplete = false;
		this.consumedTokens = new ArrayList<>();
	}
	
	/**
	 * Consumes the next token.
	 * 
	 * @param token the token to consume
	 * @return true if the token was accepted, false otherwise. In the latter case,
	 *         this grammar will be marked as completed and any subsequent call of
	 *         {@link #consumeNext(String)} or {@link #complete()} will throw
	 *         {@link IllegalStateException}
	 * @throws IllegalStateException if this grammar is already completed
	 */
	public boolean consumeNext(String token) {
		if (isComplete) {
			throw new IllegalStateException("Grammar already complete");
		}
		if (!processedOptions.isEmpty()) {
			var failed = new HashSet<Grammar>();
			for (var opt : processedOptions) {
				if (!opt.consumeNext(token)) {
					failed.add(opt);
				}
			}
			processedOptions.removeAll(failed);
			if (processedOptions.isEmpty() && failed.stream().anyMatch(f -> !f.isOnTerminalState())) {
				isComplete = true;
				return false;
			}
			if (!processedOptions.isEmpty()) {
				consumedTokens.add(token);
				return true;
			}
			processedOptions.clear();
		}
		if (state != null) {
			if (state.check(token)) {
				state.consume(token);
				optionsOfPreviousState.clear();
				optionsOfPreviousState.addAll(state.options);
				state = state.next;
				consumedTokens.add(token);
				return true;
			}
		}
		for (var option : optionsOfPreviousState) {
			option = option.freshInstance();
			if (option.consumeNext(token)) {
				processedOptions.add(option);
			}
		}
		if (!processedOptions.isEmpty()) {
			consumedTokens.add(token);
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
	 * Provides a list containing the descriptions of the next expected tokens.
	 * 
	 * @return a String list, one for each expected token
	 */
	public List<String> describeExpectedTokens() {
		if (!processedOptions.isEmpty()) {
			return processedOptions.stream()
					.flatMap(opt -> opt.describeExpectedTokens().stream())
					.collect(Collectors.toList());
		}
		var result = new ArrayList<String>();
		if (state != null) {
			result.add(state.element.describeExpectedValue());
		}
		optionsOfPreviousState.forEach(option -> result.addAll(option.describeExpectedTokens()));
		return result;
	}
	
	/**
	 * Checks if the current state of the grammar is terminal. It differs from
	 * {@link #complete()} in that it doesn't mark this grammar as complete after
	 * this is called.
	 * 
	 * @return true if the grammar is on a terminal state, false otherwise
	 */
	public boolean isOnTerminalState() {
		return state == null && (processedOptions.isEmpty() || processedOptions.stream().anyMatch(Grammar::isOnTerminalState));
	}

	/**
	 * Gets the list of tokens that have been consumed so far, in order.
	 * 
	 * @return a list of tokens
	 */
	public List<String> getConsumedTokens() {
		return consumedTokens;
	}
	
	private Grammar freshInstance() {
		return new Grammar(initialState);
	}
	
	private static class State<T> {
		private final GrammarElement<T> element;
		private State<?> next;
		private final List<Grammar> options = new ArrayList<>();
		private final Consumer<T> onResolve;
		State(GrammarElement<T> element, Consumer<T> onResolve) {
			this.element = element;
			this.onResolve = onResolve != null ? onResolve : x -> {};
		}
		boolean check(String token) {
			return element.value(token).isPresent();
		}
		void consume(String token) {
			element.value(token).ifPresent(onResolve);
		}
	}
	
	public static class GrammarBuilder {
		private GrammarBuilder() {
		}
		
		private State<?> head;
		private State<?> tail;
		
		/**
		 * Adds a new element to the grammar.
		 * 
		 * @param element the element to add
		 * @param onResolve the action to execute when this element is resolved by the grammar
		 * @return this builder
		 */
		public <T> GrammarBuilder add(GrammarElement<T> element, @Nullable Consumer<T> onResolve) {
			var state = new State<>(element, onResolve);
			if (head == null) {
				head = state;
				tail = head;
			} else {
				tail.next = state;
				tail = state;
			}
			return this;
		}

		/**
		 * Adds a new element to the grammar.
		 * 
		 * @param element the element to add
		 * @return this builder
		 */
		public <T> GrammarBuilder add(GrammarElement<T> element) {
			return add(element, null);
		}

		/**
		 * Adds options to this grammar. An option is a sub-grammar that is:
		 * 
		 * <ul>
		 * <li>Optional: as the name suggests, it means that it may or may not be occuring in the source code</li>
		 * <li>Repeatable: the same option may occur more than once in the source code</li>
		 * <li>Position-independent: an option may occur at any spot in the source code of the instruction that's being resolved</li>
		 * </ul>
		 * 
		 * @param options the options to add
		 * @return this builder
		 */
		public <T> GrammarBuilder addOptions(Grammar... options) {
			tail.options.addAll(Arrays.asList(options));
			return this;
		}
		
		/**
		 * Creates the grammar with all information previously provided.
		 * @return a new grammar
		 */
		public Grammar build() {
			return new Grammar(head);
		}
	}
	
	public static GrammarBuilder builder() {
		return new GrammarBuilder();
	}
	
	/**
	 * Creates a grammar that consists of a single element. It is equivalent to:
	 * <pre>
	 * builder().add(element, onResolve).build();
	 * </pre>
	 * @param element the unique element consititing the grammar
	 * @param onResolve the action to execute when the element is resolved by the grammar
	 * @return
	 */
	public static <T> Grammar of(GrammarElement<T> element, @Nullable Consumer<T> onResolve) {
		return builder().add(element, onResolve).build();
	}

	/**
	 * Creates a grammar that consists of a single element. It is equivalent to:
	 * <pre>
	 * builder().add(element).build();
	 * </pre>
	 * @param element the unique element consititing the grammar
	 * @param onResolve the action to execute when the element is resolved by the grammar
	 * @return
	 */
	public static <T> Grammar of(GrammarElement<T> element) {
		return of(element, null);
	}
}
