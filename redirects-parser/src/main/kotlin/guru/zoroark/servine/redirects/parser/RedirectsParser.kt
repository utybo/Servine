package guru.zoroark.servine.redirects.parser

private val PATTERN = Regex("(.+?)[\t ]+(.+?)(?:[\t ]+(\\d+))?")
private const val MATCH_GROUP_FROM = 1
private const val MATCH_GROUP_TO = 2
private const val MATCH_GROUP_STATUS_CODE = 3

public fun parseRedirections(from: String): List<Redirection> {
    if (from.isEmpty())
        return listOf()

    return from.lineSequence()
        .withIndex()
        .filter { (_, line) -> line.isNotBlank() }
        .filterNot { (_, line) -> line.startsWith('#') }
        .map { (index, line) ->
            val match = PATTERN.matchEntire(line)
                ?: parseError("line ${index + 1}", "Invalid format.")
            val statusCode = match.groups[MATCH_GROUP_STATUS_CODE]
                ?.value?.toIntOrParseError(index + 1)
            Redirection(
                match.groupValues[MATCH_GROUP_FROM],
                match.groupValues[MATCH_GROUP_TO],
                statusCode ?: DEFAULT_STATUS_CODE
            )
        }.toList()
}

private fun String.toIntOrParseError(lineIndex: Int): Int {
    try {
        return toInt()
    } catch (ex: NumberFormatException) {
        throw RedirectionsFormatException(
            "line $lineIndex", "Invalid number: $this", ex
        )
    }
}

private fun parseError(where: String, message: String): Nothing =
    throw RedirectionsFormatException(where, message)

public class RedirectionsFormatException(
    where: String,
    message: String,
    cause: Exception? = null
) : Exception("$where: $message", cause)