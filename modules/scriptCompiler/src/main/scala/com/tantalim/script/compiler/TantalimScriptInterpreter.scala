package com.tantalim.script.compiler

import com.tantalim.nodes._
import com.tantalim.script.compiler.src._
import com.tantalim.util.TantalimException
import org.antlr.v4.runtime.{ANTLRInputStream, CommonTokenStream}

import scala.collection.immutable.HashMap

/**
 * Inspired by https://github.com/bkiers/Mu/blob/master/src/main/java/mu/EvalVisitor.java
 */
class TantalimScriptInterpreter(script: String) extends TantalimScriptBaseVisitor[Value] {
  private val params = scala.collection.mutable.HashMap.empty[String, Any]
  private def parser = new TantalimScriptParser(
    new CommonTokenStream(
      new TantalimScriptLexer(
        new ANTLRInputStream(script)
      )))

  def run(params: Map[String, Any] = Map.empty): Any = {
    this.params ++= params
    val visitResult = visit(parser.start)
    visitResult.toResult
  }

  override def visitStart(ctx: TantalimScriptParser.StartContext) = {
    visit(ctx.block())
  }

  override def visitPrint(ctx: TantalimScriptParser.PrintContext) = {
    val atom = visit(ctx.atom())
    println(atom)
    Value()
  }

  override def visitIdAssignment(ctx: TantalimScriptParser.IdAssignmentContext) = {
    val variable = ctx.ID().getText
    params += variable -> visit(ctx.atom()).toResult
    Value()
  }

  override def visitFieldAssignment(ctx: TantalimScriptParser.FieldAssignmentContext) = {
    val modelName = ctx.ID(0).getText
    val fieldName = ctx.ID(1).getText
    val row = params.get(modelName).get.asInstanceOf[SmartNodeInstance]
    val value = visit(ctx.atom()).toResult
    row.set(fieldName, value match {
      // TODO Convert other types
      case doubleValue: Double => TntDecimal(doubleValue)
      case intValue: Int => TntInt(intValue)
      case _ => TntString(value.toString)
    })
    Value()
  }

  override def visitNumberAtom(ctx: TantalimScriptParser.NumberAtomContext): Value = {
    val intNumber = ctx.INT()
    if (intNumber != null) return Value(intNumber.getText.toInt)
    val doubleNumber = ctx.DOUBLE()
    if (doubleNumber != null) return Value(doubleNumber.getText.toDouble)
    Value()
  }

  override def visitIdAtom(ctx: TantalimScriptParser.IdAtomContext): Value = {
    val variableName = ctx.ID().getText
    Value(params.getOrElse(variableName, throw new TantalimException(variableName + " has not been defined", "Define the variable")))
  }

  override def visitStringAtom(ctx: TantalimScriptParser.StringAtomContext) = {
    val stringValue = ctx.STRING().getText
    Value(stringValue.substring(1, stringValue.length - 1))
  }

  override def visitParExpr(ctx: TantalimScriptParser.ParExprContext) = {
    visit(ctx.atom())
  }

  override def visitReturnStat(ctx: TantalimScriptParser.ReturnStatContext) = {
    visit(ctx.atom())
  }

  override def visitForBlock(ctx: TantalimScriptParser.ForBlockContext) = {
    val itemName = ctx.item.getText
    val listName = ctx.list.getText
    val people = params.getOrElse(listName, throw new TantalimException("Unknown variable named " + listName, "")).asInstanceOf[SmartNodeSet]
    people.foreach{ item =>
      params += itemName -> item
      visit(ctx.block())
      params -= itemName
    }

    Value()
  }

}
