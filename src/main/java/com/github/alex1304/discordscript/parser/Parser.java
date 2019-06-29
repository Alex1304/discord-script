package com.github.alex1304.discordscript.parser;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.github.alex1304.discordscript.grammar.Grammar;
import com.github.alex1304.discordscript.language.Instruction;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * Parses the input into a sequence of instructions.
 */
public class Parser {
	
	private final Set<Supplier<Instruction>> language;
	
	public Parser(Set<Supplier<Instruction>> language) {
		this.language = language;
	}
	/**
	 * Parses the given code into a list of instructions.
	 * 
	 * @param code the code to parse
	 * @return the list of parsed instructions
	 * @throws ScriptException if syntax errors were present in the code. Holds the
	 *                         list of all errors and their associated line number.
	 */
	public List<Tuple2<String, Instruction>> parse(String code) throws ScriptException {
		var process = new ParseProcess(code);
		return process.start();
	}
	
	class ParseProcess {
		private ParserState state = new DefaultState();
		private int lineNumber = 1;
		private final LinkedList<String> tokensOfCurrentInstruction = new LinkedList<>();
		private final LinkedList<Tuple2<String, Instruction>> parsedInstructions = new LinkedList<>();
		private final List<Tuple2<ParseException, Integer>> errors = new ArrayList<>();
		private final String code;
		
		private ParseProcess(String code) {
			this.code = code;
		}
		
		public List<Tuple2<String, Instruction>> start() throws ScriptException {
			code.codePoints().forEach(c -> {
				state.read(this, c);
				if (c == '\n') {
					lineNumber++;
				}
			});
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

		private Tuple2<String, Instruction> parseOne() throws ParseException {
			var grammarMap = language.stream()
					.map(Supplier::get)
					.collect(Collectors.toMap(instr -> instr.getGrammar(), Function.identity()));
			var grammars = new HashSet<>(grammarMap.keySet());
			for (var token : tokensOfCurrentInstruction) {
				var oldSet = new HashSet<>(grammars);
				grammars.removeIf(grammar -> !grammar.consumeNext(token));
				if (grammars.isEmpty()) {
					var isEndExpected = oldSet.stream().anyMatch(Grammar::isOnTerminalState);
					var expecteds = oldSet.stream()
							.flatMap(grammar -> grammar.describeExpectedTokens().stream())
							.map(s -> "`" + s + "`")
							.distinct()
							.collect(Collectors.toCollection(() -> new ArrayDeque<>()));
					if (isEndExpected) {
						expecteds.addFirst("end of line");
					}
					if (expecteds.isEmpty() || expecteds.size() > 3) {
						throw new ParseException("Unexpected token `" + token + "`");
					}
					throw new ParseException("Unexpected token `" + token + "`, expected " + String.join(" or ", expecteds));
				}
			}
			var oldSet = new HashSet<>(grammars);
			grammars.removeIf(grammar -> !grammar.complete());
			if (grammars.isEmpty()) {
				var expecteds = oldSet.stream()
						.flatMap(grammar -> grammar.describeExpectedTokens().stream())
						.map(s -> "`" + s + "`")
						.collect(Collectors.toList());
				if (expecteds.isEmpty() || expecteds.size() > 3) {
					throw new ParseException("Unexpected end of line");
				}
				throw new ParseException("Unexpected end of line, expected " + String.join(" or ", expecteds));
			}
			return grammars.stream()
					.map(g -> Tuples.of(String.join(" ", g.getConsumedTokens()), grammarMap.get(g)))
					.findAny()
					.orElseThrow();
		}
	}
}

