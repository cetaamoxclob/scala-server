package data

import java.sql.{PreparedStatement, Statement, Connection, ResultSet}

import com.tantalim.nodes._
import play.api.Play.current
import play.api.db.DB

trait DatabaseConnection {

  private def connect[T](f: Connection => T): T = {
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

  def query(sql: String, numberedParameters: List[Any]): ResultSet = {
    connect {
      connection =>
        if (numberedParameters.isEmpty) {
          val stmt = connection.createStatement
          println("Executing Query: " + sql)
          stmt.executeQuery(sql)
        } else {
          val stmt = connection.prepareStatement(sql)
          if (numberedParameters.nonEmpty) {
            setParameters(stmt, numberedParameters)
          }
          println("Executing Prepared Query: " + sql + " with " + numberedParameters)
          stmt.executeQuery
        }
    }
  }


  def update(sql: String, numberedParameters: List[Any] = List.empty): Int = {
    connect {
      connection =>
        update(sql, numberedParameters, connection)
    }
  }

  def update(sql: String, numberedParameters: List[Any], connection: Connection): Int = {
    if (numberedParameters.isEmpty) {
      val stmt = connection.createStatement
      println("Executing Update: " + sql)
      stmt.executeUpdate(sql)
    } else {
      val stmt = connection.prepareStatement(sql)
      if (numberedParameters.nonEmpty) {
        setParameters(stmt, numberedParameters)
      }
      println("Executing Prepared Update: " + sql + " with " + numberedParameters)
      stmt.executeUpdate
    }
  }

  def insert(sql: String, numberedParameters: List[Any], connection: Connection): ResultSet = {
    if (numberedParameters.isEmpty) {
      val stmt = connection.createStatement
      println("Executing Insert: " + sql)
      stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS)
      stmt.getGeneratedKeys
    } else {
      val stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
      println("Executing Prepared Insert: " + sql)
      //      if (stmt.getParameterMetaData.getParameterCount > 0) {
      //        println(" with " + stmt.getParameterMetaData.getParameterType(1))
      //      }

      if (numberedParameters.nonEmpty) {
        setParameters(stmt, numberedParameters)
      }
      stmt.executeUpdate
      stmt.getGeneratedKeys
    }
  }

  private def setParameters(stmt: PreparedStatement, numberedParameters: List[Any]): Unit = {
    numberedParameters.zipWithIndex.foreach {
      case (tntValue, zipIndex) =>
        val index = zipIndex + 1
        tntValue match {
          case value: String => stmt.setString(index, value)
          case value: Boolean => stmt.setInt(index, if (value) 1 else 0)
          case TntBoolean(value) => stmt.setInt(index, if (value) 1 else 0)
          case value: Long => stmt.setLong(index, value)
          case value: java.lang.Long => stmt.setLong(index, value)
          case value: Int => stmt.setInt(index, value)
          case value: java.lang.Integer => stmt.setInt(index, value)
          case value: Float => stmt.setFloat(index, value)
          case TntInt(value) => stmt.setInt(index, value.toInt)
          case TntDecimal(value) => stmt.setBigDecimal(index, value.bigDecimal)
          case TntString(value) => stmt.setString(index, value)
          case TntNull() => stmt.setNull(index, java.sql.Types.VARCHAR)
          case TntDate(value) => stmt.setDate(index, new java.sql.Date(value.getMillis))
          case value: org.joda.time.DateTime => stmt.setDate(index, new java.sql.Date(value.getMillis))
          case value: java.util.Date => stmt.setDate(index, new java.sql.Date(value.getTime))
          case value => throw new Exception(s"Parameters of type ${value.getClass} is not supported for value $value")
        }
    }
  }

}
