package justToTry

import scala.annotation.tailrec

object Example {

}
object Solution extends App {

 /* println(time(aVeryBigSum(Array(
    10000,20000,12312313,1321131,2,1213213123,13121,21312,3,12311,2,3,1231231,2,13123123,13213211,2,3,1312311,13213122,3,1,2,3,
    10000,20000,12312313,1321131,2,1213213123,13121,21312,3,12311,2,3,1231231,2,13123123,13213211,2,3,1312311,13213122,3,1,2,3,
    10000,20000,12312313,1321131,2,1213213123,13121,21312,3,12311,2,3,1231231,2,13123123,13213211,2,3,1312311,13213122,3,1,2,3,
    10000,20000,12312313,1321131,2,1213213123,13121,21312,3,12311,2,3,1231231,2,13123123,13213211,2,3,1312311,13213122,3,1,2,3,
    10000,20000,12312313,1321131,2,1213213123,13121,21312,3,12311,2,3,1231231,2,13123123,13213211,2,3,1312311,13213122,3,1,2,3,
    10000,20000,12312313,1321131,2,1213213123,13121,21312,3,12311,2,3,1231231,2,13123123,13213211,2,3,1312311,13213122,3,1,2,3,
    10000,20000,12312313,1321131,2,1213213123,13121,21312,3,12311,2,3,1231231,2,13123123,13213211,2,3,1312311,13213122,3,1,2,3,
    10000,20000,12312313,1321131,2,1213213123,13121,21312,3,12311,2,3,1231231,2,13123123,13213211,2,3,1312311,13213122,3,1,2,3,
    10000,20000,12312313,1321131,2,1213213123,13121,21312,3,12311,2,3,1231231,2,13123123,13213211,2,3,1312311,13213122,3,1,2,3,
    10000,20000,12312313,1321131,2,1213213123,13121,21312,3,12311,2,3,1231231,2,13123123,13213211,2,3,1312311,13213122,3,1,2,3,
    10000,20000,12312313,1321131,2,1213213123,13121,21312,3,12311,2,3,1231231,2,13123123,13213211,2,3,1312311,13213122,3,1,2,3,
    10000,20000,12312313,1321131,2,1213213123,13121,21312,3,12311,2,3,1231231,2,13123123,13213211,2,3,1312311,13213122,3,1,2,3,
    10000,20000,12312313,1321131,2,1213213123,13121,21312,3,12311,2,3,1231231,2,13123123,13213211,2,3,1312311,13213122,3,1,2,3
  ))))*/

  def compareTriplets(a: Array[Int], b: Array[Int]): Array[Int] = {
    val t = a.toList.zip(b.toList).foldLeft((0,0)){ (acc,el) =>
      if(el._1 > el._2) (acc._1+1, acc._2)
      else if(el._2 > el._1) (acc._1, acc._2 + 1)
      else acc
    }
    Array(t._1, t._2)

  }

  def aVeryBigSum(ar: Array[Long]): Long = {
    ar.toSeq.par.sum
    //ar.toSeq.sum
  }

  def time[T](t: => T) = {
    val t1 = System.currentTimeMillis()
    val result = t
    val t2 = System.currentTimeMillis()
    println(s"Time: ${t2-t1}")
    t
  }
  def diagonalDifference(arr: Array[Array[Int]]): Int = {
    // Write your code here
    val length = arr.length - 1
    val a = (0 to length).foldLeft(0){(acc, i) => acc + arr(i)(i)}
    val b = (0 to length).foldLeft(0){(acc, i) => acc + arr(length - i)(i)}
    math.abs(a-b)

  }

  //println(diagonalDifference(Array(Array(1,2,3), Array(4,5,6), Array(7,8,9))))
  // Complete the larrysArray function below.
  def rotateLeftOld(A: Array[Int], currentIndex: Int): Array[Int] = {
    val itemsToRotateMinus1 = 2
    (0 to A.length - 1).map{ i =>
      if(i < currentIndex) A(i)
      else if(i > currentIndex + itemsToRotateMinus1) A(i)
      else if(i == currentIndex + itemsToRotateMinus1) A(i - itemsToRotateMinus1)
      else A(i + 1)
    }.toArray
  }
  def rotateLeft(A: Array[Int]): Array[Int] = {
    val itemsToRotateMinus1 = 2
    (0 to A.length - 1).map{ i =>
      if(i == itemsToRotateMinus1) A(i - itemsToRotateMinus1)
      else A(i + 1)
    }.toArray
  }

  def rotateRightOld(A: Array[Int], currentIndex: Int): Array[Int] = {
    val itemsToRotateMinus1 = 2
    (0 to A.length - 1).map{ i =>
      if(i < currentIndex) A(i)
      else if(i > currentIndex + itemsToRotateMinus1) A(i)
      else if(i == currentIndex) A(i + itemsToRotateMinus1)
      else A(i - 1)
    }.toArray
  }

  def rotateRight(A: Array[Int]): Array[Int] = {
    val itemsToRotateMinus1 = 2
    (0 to A.length - 1).map{ i =>
      if(i == 0) A(i + itemsToRotateMinus1)
      else A(i - 1)
    }.toArray
  }

  @tailrec
 /* def calculateLarrysArrayNew(A: Array[Int], currentIndex: Int): Boolean = {
    val currentValue = currentIndex + 1
    A.indexOf(currentIndex+1) match {
      case x if(x == currentIndex) =>
        if(currentIndex == A.length - 1) true
        else calculateLarrysArrayNew(A, currentIndex + 1)
      case x if(x == currentIndex + 1) =>
        if(currentIndex == A.length - 2) false
        else {
          val newA: Array[Int] = rotateLeft(A, currentIndex)
          calculateLarrysArrayNew(newA, currentIndex + 1)
        }
      case x if(x == currentIndex + 2) =>
        val newA: Array[Int] = rotateRight(A, currentIndex)
        calculateLarrysArrayNew(newA, currentIndex + 1)
      case x: Int =>
        val newA: Array[Int]= rotateRight(A, x - 2)
        calculateLarrysArrayNew(newA, currentIndex)
    }
  }
*/
  def calculateLarrysArray(A: Array[Int], currentValue: Int): Boolean = {
    A.indexOf(currentValue) match {
      case 0 =>
        if(A.length == 1) true
        else calculateLarrysArray(A.drop(1), currentValue + 1)
      case 1 =>
        if(A.length == 2) false
        else {
          val newA: Array[Int] = rotateLeft(A.slice(0, 3)) ++ A.slice(3, A.length)
          calculateLarrysArray(newA.drop(1), currentValue + 1)
        }
      case 2 =>
        val newA: Array[Int] = rotateRight(A.slice(0, 3)) ++ A.slice(3, A.length)
        calculateLarrysArray(newA.drop(1), currentValue + 1)
      case x: Int =>
        val newA: Array[Int]= A.slice(0, x - 2) ++ rotateRight(A.slice(x - 2, x + 1)) ++ A.slice(x + 1, A.length)
        calculateLarrysArray(newA, currentValue)
    }
  }
  def larrysArray(A: Array[Int]): String = {
    if(calculateLarrysArray(A, 1)) "YES"
    else "NO"
  }

  println(larrysArray(Array(3,1,2))) // YES
  println(larrysArray(Array(1,3,4,2)))  // YES
  println(larrysArray(Array(1,2,3,5,4)))  // NO
  println(larrysArray(Array(1,2,3,4,5)))  // YES
  println(larrysArray(Array(2,3,4,1))) //NO
  println(larrysArray(Array(2,4,3,1))) //YES


  println(larrysArray(Array(9,6,8,12,3,7,1,11,10,2,5,4))) // NO*/
  println(
    time(larrysArray(Array(17,21,2,1,16,9,12,11,6,18,20,7,14,8,19,10,3,4,13,5,15)))
  )// YES
  println(time(larrysArray(Array(5,8,13,3,10,4,12,1,2,7,14,6,15,11,9)))) // NO
  println(time(larrysArray(Array(8,10,6,11,7,1,9,12,3,5,13,4,2)))) // YES
  println(time(larrysArray(Array(7,9,15,8,10,16,6,14,5,13,17,12,3,11,4,1,18,2)))) // NO

}

