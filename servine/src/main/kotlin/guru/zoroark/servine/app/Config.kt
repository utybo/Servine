package guru.zoroark.servine.app

data class Config(
    val port: Int,
    val directory: String,
    val liveReload: Boolean
)