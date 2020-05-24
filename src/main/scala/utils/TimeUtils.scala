package utils

object TimeUtils {
  def time[T](t: => T) = {
    val t1 = System.currentTimeMillis()
    val result = t
    val t2 = System.currentTimeMillis()
    println(s"Time: ${t2-t1}")
    t
  }
}
