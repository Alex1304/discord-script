package com.github.alex1304.discordscript.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.alex1304.discordscript.language.Grammar;
import com.github.alex1304.discordscript.language.Instruction;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * Parses the input into a sequence of instructions.
 */
class Parser {

	private ParserState state;
	private int lineNumber;
	private final LinkedList<String> tokensOfCurrentInstruction = new LinkedList<>();
	private final LinkedList<Instruction> parsedInstructions = new LinkedList<>();
	private final List<Tuple2<ParseException, Integer>> errors = new ArrayList<>();
	private final Set<Supplier<Instruction>> language;
	
	public Parser(Set<Supplier<Instruction>> language) {
		this.language = language;
	}
	
	/**
	 * Parses the given code into a list of instructions.
	 * 
	 * @param code the code to parse
	 * @return the list of instruction resulting of the parsing
	 * @throws ScriptException if syntax errors were present in the code. Holds the
	 *                         list of all errors and their associated line number.
	 */
	public List<Instruction> parse(String code) throws ScriptException {
		state = new DefaultState();
		lineNumber = 1;
		tokensOfCurrentInstruction.clear();
		parsedInstructions.clear();
		errors.clear();
		code.codePoints().forEach(c -> state.read(this, c));
		state.complete(this);
		nextInstruction(); // in case there's no line return at end of code
		if (!errors.isEmpty()) {
			throw new ScriptException(errors);
		}
		return parsedInstructions;
	}

	void setState(ParserState state) {
		this.state = Objects.requireNonNull(state);
	}

	void addToken(String token) {
		tokensOfCurrentInstruction.add(Objects.requireNonNull(token));
	}

	void incrementLineNumber() {
		lineNumber++;
	}
	
	void nextInstruction() {
		if (!tokensOfCurrentInstruction.isEmpty()) {
			try {
				parsedInstructions.add(parseOne());
			} catch (ParseException e) {
				errors.add(Tuples.of(e, lineNumber));
			}
			tokensOfCurrentInstruction.clear();
		}
	}

	private Instruction parseOne() throws ParseException {
		var grammarMap = language.stream()
				.map(Supplier::get)
				.collect(Collectors.toMap(instr -> instr.getGrammar().freshInstance(), Function.identity()));
		var grammars = new HashSet<>(grammarMap.keySet());
		for (var token : tokensOfCurrentInstruction) {
			var oldSet = new HashSet<>(grammars);
			grammars.removeIf(grammar -> !grammar.consumeNext(token));
			if (grammars.isEmpty()) {
				var isEndExpected = oldSet.stream().anyMatch(Grammar::isOnTerminalState);
				var expecteds = oldSet.stream()
						.flatMap(grammar -> grammar.describeExpectedTokens().stream())
						.map(s -> "`" + s + "`");
				if (isEndExpected) {
					expecteds = Stream.concat(Stream.of("end of line"), expecteds);
				}
				throw new ParseException("Failed to parse `" + token + "`, expected " + expecteds
						.distinct()
						.collect(Collectors.joining(" or ")));
			}
		}
		var oldSet = new HashSet<>(grammars);
		grammars.removeIf(grammar -> !grammar.complete());
		if (grammars.isEmpty()) {
			var expecteds = oldSet.stream()
					.flatMap(grammar -> grammar.describeExpectedTokens().stream())
					.map(s -> "`" + s + "`");
			throw new ParseException("Unexpected end of line, expected " + expecteds
					.collect(Collectors.joining(" or ")));
		}
		return grammars.stream()
				.map(grammarMap::get)
				.findAny()
				.orElseThrow();
	}
}