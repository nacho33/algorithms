package larryArray

import utils.TimeUtils._

object LarryArray extends App {

  def rotateLeft(A: Array[Int]): Array[Int] = {
    val itemsToRotateMinus1 = 2
    (0 to A.length - 1).map{ i =>
      if(i == itemsToRotateMinus1) A(i - itemsToRotateMinus1)
      else A(i + 1)
    }.toArray
  }

  def rotateRight(A: Array[Int]): Array[Int] = {
    val itemsToRotateMinus1 = 2
    (0 to A.length - 1).map{ i =>
      if(i == 0) A(i + itemsToRotateMinus1)
      else A(i - 1)
    }.toArray
  }

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
