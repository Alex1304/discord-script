package com.github.alex1304.discordscript;

import java.util.Objects;

public class ScriptExecutionException extends RuntimeException {
	private static final long serialVersionUID = 5114122532037313588L;
	
	private String source;

	public ScriptExecutionException(String source, Throwable cause) {
		super(cause);
		this.source = Objects.requireNonNull(source);
	}
	
	@Override
	public String getMessage() {
		return ":no_entry_sign: **An error occured when executing `" + source + "`:**\n"
				+ "```\n"
				+ errorMessage() + "\n"
				+ "```\n"
				+ "Aborting.";
	}
	
	private String errorMessage() {
		if (getCause().getMessage() == null) {
			return "Unknown error (" + getCause().getClass().getName() + ")";
		}
		return getCause().getMessage();
	}
}
