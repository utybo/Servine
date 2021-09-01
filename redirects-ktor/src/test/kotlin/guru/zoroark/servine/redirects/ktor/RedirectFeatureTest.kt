package guru.zoroark.servine.redirects.ktor

import ch.tutteli.atrium.api.fluent.en_GB.notToBeNull
import ch.tutteli.atrium.api.fluent.en_GB.toBe
import ch.tutteli.atrium.api.verbs.expect
import guru.zoroark.servine.redirects.parser.Redirection
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Test
import io.ktor.application.install
import io.ktor.http.HttpMethod
import io.ktor.http.HttpMethod.Companion.Get
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.testing.handleRequest

fun Application.testModule() {
    install(Redirects) {
        redirections += Redirection("/from", "/to")
        redirections += Redirection("/from/np", "/to/np", 302)
        redirections += Redirection("/from/404", "/to/404", 404)
        redirections += Redirection("/from/200", "/to/200", 200)
        redirections += Redirection("/from/200/actually403", "/to/200/actually403", 200)
        redirections += Redirection("/from/splat", "/to", 200, true)
    }

    routing {
        get("/to") {
            call.respond(HttpStatusCode.OK, "We good!")
        }

        get("/to/404") {
            call.respond(HttpStatusCode.OK, "My custom 404 rewrite")
        }

        route("/to/200") {
            get {
                call.respond(HttpStatusCode.OK, "My custom 200 rewrite")
            }
            get("/actually403") {
                call.respond(HttpStatusCode.Forbidden, "No >:(")

            }
        }
    }
}

class RedirectFeatureTest {
    @Test
    fun `Regular GET call`() {
        withTestApplication(Application::testModule) {
            handleRequest(Get, "/to").apply {
                expect(response.status()).toBe(HttpStatusCode.OK)
                expect(response.content).notToBeNull().toBe("We good!")
            }
        }
    }

    @Test
    fun `Simple redirection`() {
        withTestApplication(Application::testModule) {
            handleRequest(Get, "/from").apply {
                expect(response.status()).toBe(HttpStatusCode.MovedPermanently)
            }
        }
    }

    @Test
    fun `Simple redirection 302`() {
        withTestApplication(Application::testModule) {
            handleRequest(Get, "/from/np").apply {
                expect(response.status()).toBe(HttpStatusCode.Found)
            }
        }
    }

    @Test
    fun `Simple rewrite 404`() {
        withTestApplication(Application::testModule) {
            handleRequest(Get, "/from/404").apply {
                expect(response.status()).toBe(HttpStatusCode.NotFound)
                expect(response.content).notToBeNull()
                    .toBe("My custom 404 rewrite")
            }
        }
    }

    @Test
    fun `Simple rewrite 200`() {
        withTestApplication(Application::testModule) {
            handleRequest(Get, "/from/200").apply {
                expect(response.status()).toBe(HttpStatusCode.OK)
                expect(response.content).notToBeNull()
                    .toBe("My custom 200 rewrite")
            }
        }
    }

    @Test
    fun `Simple rewrite 200 with different status return`() {
        withTestApplication(Application::testModule) {
            handleRequest(Get, "/from/200/actually403").apply {
                expect(response.status()).toBe(HttpStatusCode.Forbidden)
                expect(response.content).notToBeNull().toBe("No >:(")
            }
        }
    }

    @Test
    fun `Splat redirect from base`() {
        withTestApplication(Application::testModule) {
            handleRequest(Get, "/from/splat").apply {
                expect(response.status()).toBe(HttpStatusCode.OK)
                expect(response.content).notToBeNull().toBe("We good!")
            }
        }
    }
    @Test
    fun `Splat redirect from sub`() {
        withTestApplication(Application::testModule) {
            handleRequest(Get, "/from/splat/yes/no/maybe").apply {
                expect(response.status()).toBe(HttpStatusCode.OK)
                expect(response.content).notToBeNull().toBe("We good!")
            }
        }
    }
}
