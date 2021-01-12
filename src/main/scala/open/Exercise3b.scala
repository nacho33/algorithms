package open

import org.scalameter._

import scala.annotation.tailrec

object Exercise3b extends App {

  val standardConfig = config(
    Key.exec.minWarmupRuns -> 5,
    Key.exec.maxWarmupRuns -> 10,
    Key.exec.benchRuns -> 10,
    Key.verbose -> true
  ) withWarmer(new Warmer.Default)


  def existsMin(zipped: Array[(Int, Int)]): Boolean = {
    zipped.exists(tuple => Math.abs(tuple._2 - tuple._1) == 1)
  }

  @tailrec
  def guessMinDistance(a: Array[Int], distanceIndex: Int): Int = {
    if(a.length < (distanceIndex + 1)) - 1
    else {
      val a1 = a.drop(distanceIndex)
      val a2 = a.dropRight(distanceIndex)
      val zipped: Array[(Int, Int)] = a1.zip(a2)
      val calculatedDistance = existsMin(zipped)
      if(calculatedDistance) distanceIndex
      else guessMinDistance(a, distanceIndex + 1)

    }
  }

  def solution(a: Array[Int]): Int = {
    // write your code in Scala 2.12
    val distinctValues = a.distinct.sorted
    val translatedValues = (Int.MinValue to Int.MinValue + distinctValues.length)
    val zippedDistinctValues: Map[Int, Int] = distinctValues.zip(translatedValues).toMap
    val newA: Array[Int] = a.map(zippedDistinctValues)
    if(a.length < 2 ) -1
    else guessMinDistance(newA, 1)

  }
val a = standardConfig measure {
  solution(Array(0,3,3,3,3,3,3,3,3,888888888,888888888,3,3,3,3,3,3,7,7,5,5,3,3,3,11,1))
}
  println(a)
 /* TimeUtils.time(println(solution(Array(1))))  // -1
  TimeUtils.time(println(solution(Array(1,4,7,3,3,5))))   // 2
  TimeUtils.time(println(solution(Array(0,3,3,7,5,3,11,1))) )  // 1
  TimeUtils.time(println(solution(Array(0,1))) )  // 1
  TimeUtils.time(println(solution(Array(-1,-2,3,7,5,3,11,1))) )  // 1
  TimeUtils.time(println(solution(Array(-1,5,0,4,11,11,1))) )  // 2
  TimeUtils.time(println(solution(Array(1,1))) )  // -1
  TimeUtils.time(println(solution(Array(-11,-11))) )  // -1

   TimeUtils.time(println(solution(Array(0,3,3,3,3,3,3,3,3,888888888,888888888,3,3,3,3,3,3,7,7,5,5,3,3,3,11,1))) )  // 1
   TimeUtils.time(println(solution(Array(3,3,3,3,3,3,3,3))) )  // -1

  TimeUtils.time(println(solution(Array(3,3,7,1,5))) )  // 2
  TimeUtils.time(println(solution(Array())) )  // -1

*/
}
