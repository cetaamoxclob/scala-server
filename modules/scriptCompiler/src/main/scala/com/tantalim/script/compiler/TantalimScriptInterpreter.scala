package com.tantalim.script.compiler

import com.tantalim.script.compiler.src._
import org.antlr.v4.runtime.{ANTLRInputStream, CommonTokenStream}

class TantalimScriptInterpreter(script: String) extends TantalimScriptBaseVisitor[Value] {
  private def parser = new TantalimScriptParser(
    new CommonTokenStream(
      new TantalimScriptLexer(
        new ANTLRInputStream(script)
      )))

  def run(params: Map[String, Any] = Map.empty): Any = {
    visit(parser.start).toResult()
  }

  override def visitStart(ctx: TantalimScriptParser.StartContext) = {
    visit(ctx.block())
  }

  override def visitPrint(ctx: TantalimScriptParser.PrintContext) = {
    val rawString = ctx.STRING().getText
    println(rawString)
    Value()
  }

//  override def visitStringAtom(ctx: TantalimScriptParser.StringAtomContext) = {
//    val raw = ctx.getText
//    val cleanedUp = raw.substring(1, raw.length() - 1).replace("\'", "'").replace("\\\"", "\"")
//    Value(values = List(cleanedUp))
//  }



}
