package open

object Exercise2 extends App {

  def cropMessage(message: String, k: Int): String = {
    val lastChar: Char = message.charAt(message.length - 1)
    val secondLastChar: Char = message.charAt(message.length - 2)
    if(lastChar == ' ') message.take(k)
    else if(secondLastChar == ' ') message.take(k - 1)
    else {
      message.split(" ").dropRight(1).mkString(" ")
    }
  }
  def solution(message: String, k: Int): String = {
    // write your code in Scala 2.12
    if(message.length <= k) message
    else cropMessage(message.take(k + 1), k)
  }

  //println(solution("Codility We test coders", 14) == "Codility We")
  //println(solution("Why not", 100) == "Why not")
  //println(solution("The quick brown fox jumps over the lazy dog", 39) == "The quick brown fox jumps over the lazy")
  //println(solution("a", 1) == "a")
  //println(solution("ab", 1) == "")
  //println(solution("a b", 1) == "a")
  //println(solution("abc", 2) == "")
  //println(solution("a c", 2) == "a")
  //println(solution("ab c", 2) == "ab")
  //println(solution("a c", 2) == "a")
  //



}
