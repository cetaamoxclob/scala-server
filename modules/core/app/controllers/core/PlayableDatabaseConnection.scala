package controllers.core

import java.sql.Connection

import com.tantalim.database.data.DatabaseConnection
import play.api.Play.current
import play.api.db.DB

trait PlayableDatabaseConnection extends DatabaseConnection {

  def getConnection: Connection = {
    DB.getConnection()
  }

}
