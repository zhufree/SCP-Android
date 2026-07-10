package info.free.scp.data.reading

import info.free.scp.SCPConstants.ScpType.SAVE_JOKE
import info.free.scp.SCPConstants.ScpType.SAVE_JOKE_CN
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES
import info.free.scp.SCPConstants.ScpType.SAVE_SERIES_CN

data class ReadListImportRequest(
    val scpType: Int,
    val numberPattern: String
)

object ReadListImportParser {
    private val separators = Regex("[,，;；\\s]+")

    fun parse(input: String): List<ReadListImportRequest> {
        return input
            .split(separators)
            .mapNotNull(::parseToken)
    }

    private fun parseToken(rawToken: String): ReadListImportRequest? {
        val token = rawToken.trim()
        if (token.isEmpty()) {
            return null
        }

        val isCn = token.contains("cn", ignoreCase = true)
        val isJoke = token.contains("j", ignoreCase = true)
        val digits = token
            .filter(Char::isDigit)
        if (digits.isEmpty()) {
            return null
        }
        val number = digits.padStart(3, '0')

        val scpType = when {
            isCn && isJoke -> SAVE_JOKE_CN
            isCn -> SAVE_SERIES_CN
            isJoke -> SAVE_JOKE
            else -> SAVE_SERIES
        }
        val pattern = if (isJoke) {
            "%-$number-%"
        } else {
            "%-$number %"
        }
        return ReadListImportRequest(scpType, pattern)
    }
}
