package guru.zoroark.servine.redirects.ktor

import guru.zoroark.servine.redirects.ktor.Redirects.Feature.Redirection
import guru.zoroark.servine.redirects.parser.Redirection
import guru.zoroark.servine.redirects.parser.parseRedirections
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import java.nio.file.Files
import java.nio.file.Path

/**
 * Automatic redirection feature based on a set of rules that can be hard-coded
 * or read from a `_redirects` file.
 *
 * Here's an example on how to use this feature/plugin:
 *
 * ```
 * install(Redirects) {
 *      // Use redirections from a `redirects_` file
 *      from(Path.of("some/directory/_redirects"))
 *      // Manually add redirections
 *      redirections += Redirection("/from/this", "/to/that")
 * }
 * ```
 *
 * ### Shadowing
 *
 * The feature intercepts different phases depending on whether redirections are
 * shadowing or not.
 *
 * * Non-shadowing redirections (which is the default behavior) are handled just
 *   before the Fallback phase in the application call pipeline. This means that
 *   these redirections are used as a last resort.
 * * Shadowing redirections (where the redirection code is followed by a `!` in
 *   the `_redirects` syntax, or by setting [Redirection.shadow]) are not
 *   available yet.
 *
 * ### Redirection behavior
 *
 * 301 and 302 status code redirections simply send a redirection response with
 * a regular [respondRedirect] call.
 *
 * Rewrites (e.g., 404 and 200 status codes) are done by replacing the internal
 * call's connection point and re-firing a pipeline execution using this new
 * call.
 */
public class Redirects(configuration: Configuration) {
    private val redirects = configuration.redirections.toList()

    public companion object Feature :
        ApplicationFeature<ApplicationCallPipeline, Configuration, Redirects> {
        override val key: AttributeKey<Redirects> = AttributeKey("Redirects")
        private val Redirection = PipelinePhase("Redirection")
        internal val OverrideStatusCode =
            AttributeKey<HttpStatusCode>("OverrideStatusCode")
        public val DoNotRedirect: AttributeKey<Unit> =
            AttributeKey("DoNotRedirect")

        override fun install(
            pipeline: ApplicationCallPipeline,
            configure: Configuration.() -> Unit
        ): Redirects {
            val feature = Redirects(Configuration().apply(configure))
            pipeline.insertPhaseBefore(
                ApplicationCallPipeline.Fallback,
                Redirection
            )
            pipeline.intercept(Redirection) { feature.fallbackIntercepted(this) }

            pipeline.sendPipeline.intercept(ApplicationSendPipeline.Before) {
                feature.sendIntercepted(this)
            }
            return feature
        }
    }

    private suspend fun fallbackIntercepted(
        ctx: PipelineContext<Unit, ApplicationCall>
    ): Unit = with(ctx) {
        if (call.attributes.getOrNull(DoNotRedirect) != null)
            return
        val rule = redirects.firstOrNull { it.matches(call) } ?: return
        respondTo(rule)
        finish()
    }

    private suspend fun PipelineContext<Unit, ApplicationCall>.respondTo(rule: Redirection) {
        when (rule.statusCode) {
            301 -> call.respondRedirect(rule.to, true)
            302 -> call.respondRedirect(rule.to, false)
            404 -> call.redoWithUrl(rule.to, HttpStatusCode.NotFound)
            200 -> call.redoWithUrl(rule.to)
            // TODO else -> error
        }
    }

    private suspend fun ApplicationCall.redoWithUrl(
        to: String,
        overrideStatusCode: HttpStatusCode? = null
    ) {
        // From https://stackoverflow.com/a/63301169
        val connectionPoint =
            object : RequestConnectionPoint by this.request.local {
                override val uri: String = to
            }
        val req = object : ApplicationRequest by this.request {
            override val local = connectionPoint
        }
        val newCall = object : ApplicationCall by this {
            override val request: ApplicationRequest = req
        }
        if (overrideStatusCode != null)
            newCall.attributes.put(OverrideStatusCode, overrideStatusCode)
        this.application.execute(newCall, Unit)
    }

    private suspend fun sendIntercepted(
        ctx: PipelineContext<Any, ApplicationCall>
    ) = with(ctx) {
        call.attributes.put(DoNotRedirect, Unit)
        call.attributes.getOrNull(OverrideStatusCode)?.let { override ->
            call.response.status(override)
        }
    }

    public class Configuration {
        /**
         * Mutable list of the redirections this feature should use.
         */
        public val redirections: MutableList<Redirection> = mutableListOf<Redirection>()
    }
}

private fun Redirection.matches(call: ApplicationCall): Boolean {
    val path = call.request.path()
    return when {
        path == from -> true
        splat -> {
            val fromComponents = from.split('/').filter { it.isNotEmpty() }
            val pathComponents = path.split('/').filter { it.isNotEmpty() }
                .take(fromComponents.size)
            fromComponents == pathComponents
        }
        else -> false
    }
}

/**
 * Parses and adds redirection rules from the given path. This path may denote
 * any readable file.
 */
public fun Redirects.Configuration.from(path: Path) {
    val fileContent = Files.readString(path)
    val redirects = parseRedirections(fileContent)
    redirections += redirects
}
