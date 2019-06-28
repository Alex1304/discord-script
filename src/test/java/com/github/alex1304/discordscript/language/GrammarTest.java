package com.github.alex1304.discordscript.language;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GrammarTest {
	
	private Grammar g1, g2, g3, g4;

	@BeforeEach
	void setUp() throws Exception {
		g1 = Grammar.builder()
				.append(new Keyword("create"))
				.append(new Keyword("text"))
				.append(new Keyword("channel"))
				.append(new StringInput("", v -> !v.startsWith("a")))
				.build();
		g2 = Grammar.builder()
				.append(new Keyword("create"))
				.append(new Keyword("text"))
				.append(new Keyword("channel"))
				.append(new StringInput(""))
				.optional()
				.append(new Keyword("nsfw"))
				.build();
		g3 = Grammar.builder()
				.append(new Keyword("create"))
				.append(new Keyword("text"))
				.append(new Keyword("channel"))
				.append(new StringInput(""))
				.optional()
				.appendSubgrammar(Grammar.builder()
						.append(new Keyword("with"))
						.append(new Keyword("topic"))
						.append(new StringInput("")), false)
				.build();
		g4 = Grammar.builder()
				.append(new Keyword("edit"))
				.append(new Keyword("permissions"))
				.append(new Keyword("for"))
				.append(new StringInput(""))
				.append(new Keyword("default"))
				.append(new StringInput(""))
				.appendSubgrammar(Grammar.builder()
						.append(new Keyword("in"))
						.append(new StringInput(""))
						.append(new Keyword("allow", "deny"))
						.append(new StringInput("")), true)
				.build();
	}

	@Test
	void testBaseCase() {
		assertTrue(g1.consumeNext("create"), "base case, token 1");
		assertTrue(g1.consumeNext("text"), "base case, token 2");
		assertTrue(g1.consumeNext("channel"), "base case, token 3");
		assertTrue(g1.consumeNext("test"), "base case, token 4");
		assertTrue(g1.complete(), "base case, complete");
	}

	@Test
	void testCaseInsensitive() {
		assertTrue(g1.consumeNext("CreAtE"), "case insensitive, token 1");
		assertTrue(g1.consumeNext("TeXt"), "case insensitive, token 2");
		assertTrue(g1.consumeNext("chANNel"), "case insensitive, token 3");
		assertTrue(g1.consumeNext("TeSt"), "case insensitive, token 4");
		assertTrue(g1.complete(), "base case, complete");
	}

	@Test
	void testToken1Incorrect() {
		assertFalse(g1.consumeNext("delete"), "token 1 incorrect, token 1");
		assertThrows(IllegalStateException.class, () -> g1.consumeNext("text"), "token 1 incorrect, token 2");
		assertThrows(IllegalStateException.class, () -> g1.consumeNext("channel"), "token 1 incorrect, token 3");
		assertThrows(IllegalStateException.class, () -> g1.consumeNext("test"), "token 1 incorrect, token 4");
		assertThrows(IllegalStateException.class, () -> g1.complete(), "token 1 incorrect, complete");
	}

	@Test
	void testToken2Incorrect() {
		assertTrue(g1.consumeNext("create"), "token 2 incorrect, token 1");
		assertFalse(g1.consumeNext("voice"), "token 2 incorrect, token 2");
		assertThrows(IllegalStateException.class, () -> g1.consumeNext("channel"), "token 2 incorrect, token 3");
		assertThrows(IllegalStateException.class, () -> g1.consumeNext("test"), "token 2 incorrect, token 4");
		assertThrows(IllegalStateException.class, () -> g1.complete(), "token 2 incorrect, complete");
	}

	@Test
	void testToken3Incorrect() {
		assertTrue(g1.consumeNext("create"), "token 3 incorrect, token 1");
		assertTrue(g1.consumeNext("text"), "token 3 incorrect, token 2");
		assertFalse(g1.consumeNext("shit"), "token 3 incorrect, token 3");
		assertThrows(IllegalStateException.class, () -> g1.consumeNext("test"), "token 3 incorrect, token 4");
		assertThrows(IllegalStateException.class, () -> g1.complete(), "token 3 incorrect, complete");
	}

	@Test
	void testToken4Incorrect() {
		assertTrue(g1.consumeNext("create"), "token 4 incorrect, token 1");
		assertTrue(g1.consumeNext("text"), "token 4 incorrect, token 2");
		assertTrue(g1.consumeNext("channel"), "token 4 incorrect, token 3");
		assertFalse(g1.consumeNext("alex"), "token 4 incorrect, token 4");
		assertThrows(IllegalStateException.class, () -> g1.complete(), "token 4 incorrect, complete");
	}

	@Test
	void testCompleteEarly() {
		assertTrue(g1.consumeNext("create"), "complete early, token 1");
		assertTrue(g1.consumeNext("text"), "complete early, token 2");
		assertFalse(g1.complete(), "complete early, complete");
	}

	@Test
	void testOptionalProvided() {
		assertTrue(g2.consumeNext("create"), "optional provided, g2 token 1");
		assertTrue(g2.consumeNext("text"), "optional provided, g2 token 2");
		assertTrue(g2.consumeNext("channel"), "optional provided, g2 token 3");
		assertTrue(g2.consumeNext("test"), "optional provided, g2 token 4");
		assertTrue(g2.consumeNext("nsfw"), "optional provided, g2 token 5");
		assertTrue(g2.complete(), "optional provided, g2 complete");
		
		assertTrue(g3.consumeNext("create"), "optional provided, g3 token 1");
		assertTrue(g3.consumeNext("text"), "optional provided, g3 token 2");
		assertTrue(g3.consumeNext("channel"), "optional provided, g3 token 3");
		assertTrue(g3.consumeNext("test"), "optional provided, g3 token 4");
		assertTrue(g3.consumeNext("with"), "optional provided, g3 token 5");
		assertTrue(g3.consumeNext("topic"), "optional provided, g3 token 6");
		assertTrue(g3.consumeNext("some channel topic"), "optional provided, g3 token 7");
		assertTrue(g3.complete(), "optional provided, g3 complete");
	}

	@Test
	void testOptionalNotProvided() {
		assertTrue(g2.consumeNext("create"), "optional not provided, g2 token 1");
		assertTrue(g2.consumeNext("text"), "optional not provided, g2 token 2");
		assertTrue(g2.consumeNext("channel"), "optional not provided, g2 token 3");
		assertTrue(g2.consumeNext("test"), "optional not provided, g2 token 4");
		assertTrue(g2.complete(), "optional not provided, g2 complete");
		
		assertTrue(g3.consumeNext("create"), "optional not provided, g3 token 1");
		assertTrue(g3.consumeNext("text"), "optional not provided, g3 token 2");
		assertTrue(g3.consumeNext("channel"), "optional not provided, g3 token 3");
		assertTrue(g3.consumeNext("test"), "optional not provided, g3 token 4");
		assertTrue(g3.complete(), "optional not provided, g3 complete");
	}
	
	@Test
	void testNoRepeat() {
		assertTrue(g4.consumeNext("edit"), "no repeat, token 1");
		assertTrue(g4.consumeNext("permissions"), "no repeat, token 2");
		assertTrue(g4.consumeNext("for"), "no repeat, token 3");
		assertTrue(g4.consumeNext("test"), "no repeat, token 4");
		assertTrue(g4.consumeNext("default"), "no repeat, token 5");
		assertTrue(g4.consumeNext("test"), "no repeat, token 6");
		assertTrue(g4.consumeNext("in"), "no repeat, token 7");
		assertTrue(g4.consumeNext("test"), "no repeat, token 8");
		assertTrue(g4.consumeNext("allow"), "no repeat, token 9");
		assertTrue(g4.consumeNext("5"), "no repeat, token 10");
		assertTrue(g4.complete(), "no repeat, complete");
	}
	
	@Test
	void testRepeat() {
		assertTrue(g4.consumeNext("edit"), "repeat, token 1");
		assertTrue(g4.consumeNext("permissions"), "repeat, token 2");
		assertTrue(g4.consumeNext("for"), "repeat, token 3");
		assertTrue(g4.consumeNext("test"), "repeat, token 4");
		assertTrue(g4.consumeNext("default"), "repeat, token 5");
		assertTrue(g4.consumeNext("test"), "repeat, token 6");
		assertTrue(g4.consumeNext("in"), "repeat, token 7");
		assertTrue(g4.consumeNext("test"), "repeat, token 8");
		assertTrue(g4.consumeNext("allow"), "repeat, token 9");
		assertTrue(g4.consumeNext("5"), "repeat, token 10");
		assertTrue(g4.consumeNext("in"), "repeat, token 11");
		assertTrue(g4.consumeNext("test2"), "repeat, token 12");
		assertTrue(g4.consumeNext("deny"), "repeat, token 13");
		assertTrue(g4.consumeNext("4"), "repeat, token 14");
		assertTrue(g4.complete(), "repeat, complete");
	}
	
	@Test
	void testRepeatCompleteEarly() {
		assertTrue(g4.consumeNext("edit"), "repeat complete early, token 1");
		assertTrue(g4.consumeNext("permissions"), "repeat complete early, token 2");
		assertTrue(g4.consumeNext("for"), "repeat complete early, token 3");
		assertTrue(g4.consumeNext("test"), "repeat complete early, token 4");
		assertTrue(g4.consumeNext("default"), "repeat complete early, token 5");
		assertTrue(g4.consumeNext("test"), "repeat complete early, token 6");
		assertTrue(g4.consumeNext("in"), "repeat complete early, token 7");
		assertTrue(g4.consumeNext("test"), "repeat complete early, token 8");
		assertTrue(g4.consumeNext("allow"), "repeat complete early, token 9");
		assertTrue(g4.consumeNext("5"), "repeat complete early, token 10");
		assertTrue(g4.consumeNext("in"), "repeat complete early, token 11");
		assertTrue(g4.consumeNext("test2"), "repeat complete early, token 12");
		assertFalse(g4.complete(), "repeat complete early, complete");
	}
}
