package com.tantalim.models

case class Database(name: String, dbName: Option[String] = None) extends Artifact
