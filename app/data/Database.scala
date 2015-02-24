package data

import java.sql.{PreparedStatement, Statement, Connection, ResultSet}

import play.api.Play.current
import play.api.db.DB

trait Database {

  private def connect[T](f: Connection => T): T = {
    val connection = DB.getConnection()
    try {
      f(connection)
    } finally {
      connection.close
    }
  }

  def query(sql: String, numberedParameters: List[Any]): ResultSet = {
    connect {
      connection =>
        if (numberedParameters.isEmpty) {
          val stmt = connection.createStatement
          println("Executing Query: " + sql)
          stmt.executeQuery(sql)
        } else {
          val stmt = connection.prepareStatement(sql)
          if (!numberedParameters.isEmpty) {
            setParameters(stmt, numberedParameters)
          }
          println("Executing Prepared Query: " + sql + " with " + numberedParameters)
          stmt.executeQuery
        }
    }
  }

  def update(sql: String, numberedParameters: List[Any]): Int = {
    connect {
      connection =>
        if (numberedParameters.isEmpty) {
          val stmt = connection.createStatement
          println("Executing Update: " + sql)
          stmt.executeUpdate(sql)
        } else {
          val stmt = connection.prepareStatement(sql)
          if (!numberedParameters.isEmpty) {
            setParameters(stmt, numberedParameters)
          }
          println("Executing Prepared Update: " + sql + " with " + numberedParameters)
          stmt.executeUpdate
        }
    }
  }

  def insert(sql: String, numberedParameters: List[Any]): ResultSet = {
    connect {
      connection =>
        if (numberedParameters.isEmpty) {
          val stmt = connection.createStatement
          println("Executing Insert: " + sql)
          stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS)
          stmt.getGeneratedKeys
        } else {
          val stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
          println("Executing Prepared Insert: " + sql + " with " + numberedParameters)
          if (!numberedParameters.isEmpty) {
            setParameters(stmt, numberedParameters)
          }
          stmt.executeUpdate
          stmt.getGeneratedKeys
        }
    }
  }

  private def setParameters(stmt: PreparedStatement, numberedParameters: List[Any]): Unit = {
    numberedParameters.zipWithIndex.foreach {
      case (value, zipIndex) => {
        val index = zipIndex + 1
        value match {
          case _: String => stmt.setString(index, value.asInstanceOf[String])
          case Boolean => stmt.setBoolean(index, value.asInstanceOf[Boolean])
          case Long | _: java.lang.Long => stmt.setLong(index, value.asInstanceOf[Long])
          case Int | _: java.lang.Integer => stmt.setInt(index, value.asInstanceOf[Int])
          case Float => stmt.setFloat(index, value.asInstanceOf[Float])
          case _: java.util.Date => stmt.setDate(index, value.asInstanceOf[java.sql.Date])
          case _ => throw new Exception(s"Parameters of type ${value.getClass} is not supported for value ${value}")
        }
      }
    }

  }

}
