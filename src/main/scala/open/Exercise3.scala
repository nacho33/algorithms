package open

import org.scalameter.{Key, Warmer, config}
import utils.TimeUtils

import scala.annotation.tailrec

object Exercise3 extends App {

  val standardConfig = config(
    Key.exec.minWarmupRuns -> 5,
    Key.exec.maxWarmupRuns -> 10,
    Key.exec.benchRuns -> 10,
    Key.verbose -> true
  ) withWarmer(new Warmer.Default)

  def calculateMinDistance(listA: List[Int], listB: List[Int]): Int = {
    val allDistances = for {
      a <- listA
      b <- listB
    } yield {
      Math.abs(b-a)
    }
    allDistances.min
  }

  @tailrec
  def guessMinDistance(sortedGrouped: List[(Int, List[Int])], distance: Int): Int = {
    if(sortedGrouped.length < 2) distance
    else {
      val firstList = sortedGrouped.head._2
      val secondList = sortedGrouped(1)._2
      val calculatedDistance = calculateMinDistance(firstList, secondList)
      if(calculatedDistance == 1) 1
      else {
        val newDistance =
          if(distance == -1) calculatedDistance
          else Math.min(calculatedDistance, distance)
        guessMinDistance(sortedGrouped.drop(1), newDistance)
      }
    }
  }

  def solution(a: Array[Int]): Int = {
    // write your code in Scala 2.12

      val valuesWithIndex: Array[(Int, Int)] = a.zipWithIndex
    //println(valuesWithIndex.toList)
      val grouped: Map[Int, List[Int]] = valuesWithIndex.groupBy(_._1).map(x => (x._1, x._2.map(_._2).toList))
    //println(grouped)

    val sortedGrouped: List[(Int, List[Int])] = grouped.toList.sortBy(_._1)
    //println(sortedGrouped)

      guessMinDistance(sortedGrouped, -1)



  }

  //TimeUtils.time(println(solution(Array(1))))  // -1
  //TimeUtils.time(println(solution(Array(1,4,7,3,3,5))))   // 2
  //TimeUtils.time(println(solution(Array(0,3,3,7,5,3,11,1))) )  // 1
  /*TimeUtils.time(println(solution(Array(0,1))) )  // 1
  TimeUtils.time(println(solution(Array(-1,-2,3,7,5,3,11,1))) )  // 1
  TimeUtils.time(println(solution(Array(-1,5,0,4,11,11,1))) )  // 2
  TimeUtils.time(println(solution(Array(1,1))) )  // -1
  TimeUtils.time(println(solution(Array(-11,-11))) )  // -1

  TimeUtils.time(println(solution(Array(0,3,3,3,3,3,3,3,3,888888888,888888888,3,3,3,3,3,3,7,7,5,5,3,3,3,11,1))) )  // 1
  TimeUtils.time(println(solution(Array(3,3,3,3,3,3,3,3))) )  // -1

  TimeUtils.time(println(solution(Array(3,3,3,3,3,3,3,3))) )  // -1*/
  val a = standardConfig measure {
    solution(Array(0,3,3,3,3,3,3,3,3,888888888,888888888,3,3,3,3,3,3,7,7,5,5,3,3,3,11,1))
  }
  println(a)

}
