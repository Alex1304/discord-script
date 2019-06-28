package com.github.alex1304.discordscript.parser;

import java.util.List;

import reactor.util.function.Tuple2;

public class ScriptException extends Exception {
	private static final long serialVersionUID = 7904516721676834803L;

	private final List<Tuple2<ParseException, Integer>> errors;

	public ScriptException(List<Tuple2<ParseException, Integer>> errors) {
		this.errors = errors;
	}

	public List<Tuple2<ParseException, Integer>> getErrors() {
		return errors;
	}
	
	@Override
	public String getMessage() {
		var sb = new StringBuilder();
		for (var error : errors) {
			sb.append("At line ")
				.append(error.getT2())
				.append(": ")
				.append(error.getT1().getMessage())
				.append("\n");
		}
		return sb.toString();
	}
}
