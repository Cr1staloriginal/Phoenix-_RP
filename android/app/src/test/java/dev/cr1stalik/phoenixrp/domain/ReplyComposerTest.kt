package dev.cr1stalik.phoenixrp.domain
import org.junit.Assert.assertTrue
import org.junit.Test
class ReplyComposerTest {
 @Test fun greetingGetsGreetingResponse() { assertTrue(ReplyComposer.compose("Лис", "", "Привет!").contains("Рад(а) тебя видеть")) }
 @Test fun identityQuestionUsesCharacterName() { assertTrue(ReplyComposer.compose("Мира", "Спокойная и добрая", "Кто ты?").contains("Мира")) }
 @Test fun genericReplyIncludesUserContext() { assertTrue(ReplyComposer.compose("Рэй", "", "Пойдём гулять").contains("Пойдём гулять")) }
}
