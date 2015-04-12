package com.tantalim.artifacts

import java.util.concurrent.TimeUnit

import com.tantalim.models.DeepTable
import play.api.Play.current
import play.api.cache.Cache

import scala.concurrent.duration.Duration

trait TableCache {
  def getTableFromCache(name: String): Option[DeepTable] = {
    println("Getting table " + name + " from cache")
    Cache.getAs[DeepTable](name)
  }

  def addTableToCache(name: String, table: DeepTable): Unit = {
      Cache.set(name, table, Duration(10, TimeUnit.SECONDS))
  }
}
