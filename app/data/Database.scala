package data

import java.sql.ResultSet

import play.api.Play.current
import play.api.db.DB

trait Database {

  // This is a work in progress. Still experimenting
  def connect[R](block: => R): R = {
    val conn = DB.getConnection()
    try {
      block
    } finally {
      conn.close
    }
  }

  def query(sql: String): ResultSet = {
    val conn = DB.getConnection()
    val stmt = conn.createStatement
    try {
      stmt.executeQuery(sql)
    } finally {
      stmt.close
      conn.close
    }
  }

  def insert(sql: String): ResultSet = {
    val conn = DB.getConnection()
    val stmt = conn.createStatement
    try {
      stmt.executeQuery(sql)
      stmt.getGeneratedKeys
    } finally {
      stmt.close
      conn.close
    }
  }

}
