// Generated from /Users/trevorallred/projects/tantalim/scala/modules/scriptCompiler/src/main/scala/com/tantalim/script/compiler/TantalimScript.g4 by ANTLR 4.5
package com.tantalim.script.compiler.src;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TantalimScriptParser}.
 */
public interface TantalimScriptListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link TantalimScriptParser#start}.
	 * @param ctx the parse tree
	 */
	void enterStart(@NotNull TantalimScriptParser.StartContext ctx);
	/**
	 * Exit a parse tree produced by {@link TantalimScriptParser#start}.
	 * @param ctx the parse tree
	 */
	void exitStart(@NotNull TantalimScriptParser.StartContext ctx);
	/**
	 * Enter a parse tree produced by {@link TantalimScriptParser#block}.
	 * @param ctx the parse tree
	 */
	void enterBlock(@NotNull TantalimScriptParser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link TantalimScriptParser#block}.
	 * @param ctx the parse tree
	 */
	void exitBlock(@NotNull TantalimScriptParser.BlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link TantalimScriptParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterStat(@NotNull TantalimScriptParser.StatContext ctx);
	/**
	 * Exit a parse tree produced by {@link TantalimScriptParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitStat(@NotNull TantalimScriptParser.StatContext ctx);
	/**
	 * Enter a parse tree produced by {@link TantalimScriptParser#print}.
	 * @param ctx the parse tree
	 */
	void enterPrint(@NotNull TantalimScriptParser.PrintContext ctx);
	/**
	 * Exit a parse tree produced by {@link TantalimScriptParser#print}.
	 * @param ctx the parse tree
	 */
	void exitPrint(@NotNull TantalimScriptParser.PrintContext ctx);
	/**
	 * Enter a parse tree produced by {@link TantalimScriptParser#forBlock}.
	 * @param ctx the parse tree
	 */
	void enterForBlock(@NotNull TantalimScriptParser.ForBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link TantalimScriptParser#forBlock}.
	 * @param ctx the parse tree
	 */
	void exitForBlock(@NotNull TantalimScriptParser.ForBlockContext ctx);
}