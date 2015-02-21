package data

import java.sql.{Statement, Connection, ResultSet}

import play.api.Play.current
import play.api.db.DB

trait Database {

  // This is a work in progress. Still experimenting
  def connect[T](f: Connection => T): T = {
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
        val stmt = connection.prepareStatement(sql)
        if (!numberedParameters.isEmpty) {
          numberedParameters.zipWithIndex.foreach {
            case (value, zipIndex) => {
              val index = zipIndex + 1
              value match {
                case _: String => stmt.setString(index, value.asInstanceOf[String])
                case Long => stmt.setLong(index, value.asInstanceOf[Long])
                case Boolean => stmt.setBoolean(index, value.asInstanceOf[Boolean])
                case _: java.lang.Integer => stmt.setInt(index, value.asInstanceOf[Int])
                case Int => stmt.setInt(index, value.asInstanceOf[Int])
                case Float => stmt.setFloat(index, value.asInstanceOf[Float])
                case _: java.util.Date => stmt.setDate(index, value.asInstanceOf[java.sql.Date])
                case _ => throw new Exception(s"Parameters of type ${value.getClass} is not supported for value ${value}")
              }
            }
          }
        }
        println(sql + " with " + numberedParameters)
        stmt.executeQuery
    }
  }

  def update(sql: String): Int = {
    connect {
      conn =>
        val stmt = conn.createStatement
        stmt.executeUpdate(sql)
    }
  }

  def insert(sql: String): ResultSet = {
    connect {
      conn =>
        val stmt = conn.createStatement
        stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS)
        stmt.getGeneratedKeys
    }
  }

}

class PreparedStatement(sql: String, namedParameters: Map[String, Any]) {
  val preparedSql = ""
}