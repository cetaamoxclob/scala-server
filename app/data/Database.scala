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

  def query(sql: String): ResultSet = {
    connect {
      connection =>
        val stmt = connection.createStatement
        stmt.executeQuery(sql)
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
