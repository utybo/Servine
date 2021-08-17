package guru.zoroark.servine.redirects.parser

/**
 * The default status code for redirections when one isn't explicitly specified.
 */
public const val DEFAULT_STATUS_CODE: Int = 301

/**
 * A single redirection rule. Each non-empty, non-comment row in a `_redirects`
 * file corresponds to one `Redirection` object
 */
public data class Redirection(
    val from: String,
    val to: String,
    val statusCode: Int = DEFAULT_STATUS_CODE
)