import org.scalatest.{CancelAfterFailure, FunSuite}

class SpamClassifierTest extends FunSuite with CancelAfterFailure {

  test("merge two occurrence lists") {
    val classifier = new SpamClassifier()
    val merged = classifier.mergeTwoOccurrenceList(
      Map("foo" -> 2, "bar" -> 1, "da" -> 3),
      FlaggedBagOfWord(true, Map("foo" -> 1, "bar" -> 1, "qix" -> 1))
    )
    assert(merged.size == 4)
    assert(merged("foo") == 3)
    assert(merged("bar") == 2)
    assert(merged("da") == 3)
    assert(merged("qix") == 1)
  }

  test("should convert flagged bags of words into word occurrences by message type") {
    val bagsOfWord: List[FlaggedBagOfWord] = List(
      FlaggedBagOfWord(true, Map("vends" -> 1, "assurance" -> 1, "je" -> 1)),
      FlaggedBagOfWord(true, Map("credit" -> 1, "je" -> 1, "mandatcash" -> 1)),
      FlaggedBagOfWord(false, Map("coucou" -> 1, "ça" -> 1, "va" -> 1))
    )
    val classifier = new SpamClassifier(bagsOfWord)
    val occurrences = classifier.bagsOfWordToNumberOfOccurrencesByMsgType(bagsOfWord)
    assert(occurrences(true).size == 5)
    assert(occurrences(true)("vends") == 1)
    assert(occurrences(true)("assurance") == 1)
    assert(occurrences(true)("je") == 2)
    assert(occurrences(true)("credit") == 1)
    assert(occurrences(true)("mandatcash") == 1)

    assert(occurrences(false).size == 3)
    assert(occurrences(false)("coucou") == 1)
    assert(occurrences(false)("ça") == 1)
    assert(occurrences(false)("va") == 1)
  }

  test("should compute the probability of message's type") {
    val bagsOfWord: List[FlaggedBagOfWord] = List(
      FlaggedBagOfWord(true, Map()),
      FlaggedBagOfWord(true, Map()),
      FlaggedBagOfWord(true, Map()),
      FlaggedBagOfWord(false, Map()),
      FlaggedBagOfWord(false, Map())
    )
    val classifier = new SpamClassifier(bagsOfWord)
    assert(classifier.p(true) == 3.0 / 5.0)
    assert(classifier.p(false) == 2.0 / 5.0)
  }

  test("should compute the probability of 0.0001 when the word is unknown") {
    val bagsOfWord: List[FlaggedBagOfWord] = List(
      FlaggedBagOfWord(true, Map()), FlaggedBagOfWord(false, Map())
    )
    val classifier = new SpamClassifier(bagsOfWord)
    assert(classifier.pWord("unknownWord", true) == 0.0001)
  }

  test("should compute the probability of a word knowing the type's message") {
    val bagsOfWord: List[FlaggedBagOfWord] = List(
      FlaggedBagOfWord(true, Map("mandatcash" -> 1)),
      FlaggedBagOfWord(true, Map("foo" -> 1)),
      FlaggedBagOfWord(false, Map("coucou" -> 1, "ça" -> 1, "va" -> 1))
    )
    val classifier = new SpamClassifier(bagsOfWord)
    assert(classifier.pWord("mandatcash", true) == 1.0 / 2.0)
  }

  test("should compute the probability of a message knowing its type") {
    val bagsOfWord: List[FlaggedBagOfWord] = List(
      FlaggedBagOfWord(true, Map("mandatcash" -> 1)),
      FlaggedBagOfWord(true, Map("fucking" -> 1)),
      FlaggedBagOfWord(false, Map("coucou" -> 1, "ça" -> 1, "va" -> 1))
    )
    val classifier = new SpamClassifier(bagsOfWord)
    assert(classifier.p("Fucking mandatcash !!!", true) == 0.25)
  }

  test("should compute the probability that a message is a spam") {
    val bagsOfWord: List[FlaggedBagOfWord] = List(
      FlaggedBagOfWord(false, Map("hello" -> 1, "how" -> 1, "are" -> 1, "you" -> 1)),
      FlaggedBagOfWord(false, Map("send" -> 1, "me" -> 1, "excel" -> 1, "file" -> 1)),
      FlaggedBagOfWord(false, Map("the" -> 1, "weather" -> 1, "is" -> 1, "great" -> 1)),
      FlaggedBagOfWord(true, Map("can" -> 1, "you" -> 1, "send" -> 1, "mandatcash" -> 1)),
      FlaggedBagOfWord(true, Map("big" -> 1, "promotion" -> 1, "pills" -> 1))
    )
    val classifier = new SpamClassifier(bagsOfWord)
    assert(classifier.isSpam("Can you send money !?"))
    assert(!classifier.isSpam("How are you ? Have you take your pills ?"))
  }
}
