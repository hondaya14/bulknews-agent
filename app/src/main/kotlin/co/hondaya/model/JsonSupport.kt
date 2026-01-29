package co.hondaya.model

import kotlinx.serialization.json.Json

object JsonSupport {
    val instance: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }
}
