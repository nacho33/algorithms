package open

object exercise1 extends App {

  def solution(a: Int, b: Int): Int = {
    // write your code in Scala 2.12
    val productValue = a*b
    val binaryProductValue = productValue.toBinaryString
    binaryProductValue.toCharArray.filter(el => el == '1').length
  }

  println(solution(3,7) == 3)
  println(solution(0,7) == 0)
  println(solution(0,100000000) == 0)
  println(solution(2,2) == 1)
  println(Int.MaxValue.toLong * Int.MaxValue.toLong)


  println(solution(Int.MaxValue,Int.MaxValue))



}
