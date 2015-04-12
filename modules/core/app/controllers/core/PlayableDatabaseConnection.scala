package controllers.core

import java.sql.Connection

import com.tantalim.database.data.DatabaseConnection
import play.api.Play.current
import play.api.db.DB

trait PlayableDatabaseConnection extends DatabaseConnection {

  protected def connect[T](f: Connection => T): T = {
    val connection = DB.getConnection()
    try {
      f(connection)
    } finally {
      connection.close()
    }
  }

  def getConnection: Connection = {
    DB.getConnection()
  }

}
