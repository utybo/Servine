package guru.zoroark.servine.livereload

import com.fasterxml.jackson.core.JsonProcessingException

public class LiveReloadParsingException(message: String) : JsonProcessingException(message)