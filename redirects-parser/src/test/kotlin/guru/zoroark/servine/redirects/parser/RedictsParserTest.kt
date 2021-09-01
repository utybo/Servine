package guru.zoroark.servine.redirects.parser

import ch.tutteli.atrium.api.fluent.en_GB.*
import ch.tutteli.atrium.api.verbs.expect
import org.junit.jupiter.api.Test

class RedictsParserTest {
    @Test
    fun `Empty redirection string`() {
        val res = parseRedirections("")
        expect(res).isEmpty()
    }

    @Test
    fun `Single redirection`() {
        val res = parseRedirections(
            """
            /from /to
        """.trimIndent()
        )
        expect(res).toBe(
            listOf(
                Redirection("/from", "/to")
            )
        )
    }

    @Test
    fun `Single redirection, many spaces`() {
        val res = parseRedirections(
            """
            /from                         /to
        """.trimIndent()
        )
        expect(res).toBe(
            listOf(
                Redirection("/from", "/to")
            )
        )
    }

    @Test
    fun `Single redirection, many tabs`() {
        val res = parseRedirections(
            """
            /from!!!!!/to
            """.trimIndent().replace('!', '\t')
        )
        expect(res).toBe(
            listOf(
                Redirection("/from", "/to")
            )
        )
    }

    @Test
    fun `Single redirection with status code`() {
        val res = parseRedirections(
            """
            /from /to 200
            """.trimIndent()
        )
        expect(res).toBe(
            listOf(
                Redirection("/from", "/to", 200)
            )
        )
    }

    @Test
    fun `Wrong single redirection, missing to`() {
        expect {
            parseRedirections("/fromButWhereIsTo")
        }.toThrow<RedirectionsFormatException>()
            .message.toBe("line 1: Invalid format.")
    }

    @Test
    fun `Many simple redirections`() {
        val res = parseRedirections(
            """
            /fromOne         /toTwo
            /fromThree       /toFour
            /fromFive!!!!!!!!!!/toSix
            """.trimIndent().replace('!', '\t')
        )
        expect(res).toBe(
            listOf(
                Redirection("/fromOne", "/toTwo"),
                Redirection("/fromThree", "/toFour"),
                Redirection("/fromFive", "/toSix")
            )
        )
    }

    @Test
    fun `Many simple redirections with blank lines`() {
        val res = parseRedirections(
            """
            
            
            /fromOne         /toTwo
            ! ! !
            
            /fromThree       /toFour
             ! ! !
            !!!
            
            /fromFive!!!!!!!!!!/toSix
            
            """.trimIndent().replace('!', '\t')
        )
        expect(res).toBe(
            listOf(
                Redirection("/fromOne", "/toTwo"),
                Redirection("/fromThree", "/toFour"),
                Redirection("/fromFive", "/toSix")
            )
        )
    }

    @Test
    fun `Wrong many redirections, error on line`() {
        expect {
            parseRedirections(
                """
                /fromOne       /toTwo
                /fromThree/toFourWhoops
                /fromFive      /toSix
                """.trimIndent()
            )
        }.toThrow<RedirectionsFormatException>()
            .message.toBe("line 2: Invalid format.")
    }

    @Test
    fun `Many simple redirections with comments`() {
        val res = parseRedirections(
            """
            # Hello There!
            /fromOne         /toTwo
            /fromThree       /toFour
            # This is a comment
            #/fromBlahblah /toWooloo
            /fromFive!!!!!!!!!!/toSix
            """.trimIndent().replace('!', '\t')
        )
        expect(res).toBe(
            listOf(
                Redirection("/fromOne", "/toTwo"),
                Redirection("/fromThree", "/toFour"),
                Redirection("/fromFive", "/toSix")
            )
        )
    }

    @Test
    fun `Correct splat`() {
        val res = parseRedirections(
            """
            /*          /allSplat
            /some/*     /notAllSplat
            /wow/*      /splatWithStatus    123
            """.trimIndent()
        )
        expect(res).toBe(
            listOf(
                Redirection("/", "/allSplat", splat = true),
                Redirection("/some", "/notAllSplat", splat = true),
                Redirection("/wow", "/splatWithStatus", 123, true)
            )
        )
    }
}
