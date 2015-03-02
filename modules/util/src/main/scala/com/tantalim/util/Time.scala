package com.tantalim.util

trait Timer {

  def timer[R](block: => R): R = timer("")(block)

  def timer[R](description: String)(block: => R): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    val difference = t1 - t0
    val humanTime = if (difference < 1000000) difference + " ns"
    else if (difference < 1000000000) difference/1000000 + " ms"
    else difference/1000000000 + " s"
    println(description + " Elapsed time: " + humanTime)
    result
  }


}
