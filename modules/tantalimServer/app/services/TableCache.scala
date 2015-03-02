package services

import com.tantalim.models.DeepTable
import play.api.cache.Cache
import play.api.Play.current

trait TableCache {
  def getTableFromCache(name: String): Option[DeepTable] = {
    println("Getting table " + name + " from cache")
    Cache.getAs[DeepTable](name)
  }

  def addTableToCache(name: String, table: DeepTable): Unit = {
      Cache.set(name, table)
  }

}
