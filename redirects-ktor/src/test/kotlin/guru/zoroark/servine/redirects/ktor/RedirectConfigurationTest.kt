package guru.zoroark.servine.redirects.ktor

import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import guru.zoroark.servine.redirects.parser.Redirection
import org.junit.jupiter.api.Test
import java.nio.file.Files

class RedirectConfigurationTest {
    @Test
    fun `Test configuration from file path`() {
        val fs = Jimfs.newFileSystem(Configuration.unix())
        val redirectsFile = fs.getPath("_redirects")
        Files.writeString(
            redirectsFile, """
            /one        /two 123
            /three /four     456
        """.trimIndent()
        )

        val configuration = Redirects.Configuration().apply {
            from(redirectsFile)
        }

        expect(configuration.redirections).toBe(
            mutableListOf(
                Redirection("/one", "/two", 123),
                Redirection("/three", "/four", 456)
            )
        )
    }
}
