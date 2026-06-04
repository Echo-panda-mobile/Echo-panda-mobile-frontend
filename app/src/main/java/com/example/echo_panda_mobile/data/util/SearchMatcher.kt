package com.example.echo_panda_mobile.data.util

/**
 * Flexible text search: word order does not matter ("bad desire" matches "Desire Bad"),
 * partial words, prefix matches, light typo tolerance, and letter-order (subsequence) matching.
 */
object SearchMatcher {

    fun matches(query: String, vararg fields: String?): Boolean {
        val normalizedQuery = normalize(query)
        if (normalizedQuery.isEmpty()) return true

        val haystacks = fields.mapNotNull { it?.let(::normalize) }.filter { it.isNotEmpty() }
        if (haystacks.isEmpty()) return false

        val combined = haystacks.joinToString(" ")

        if (haystacks.any { it.contains(normalizedQuery) } || combined.contains(normalizedQuery)) {
            return true
        }

        val queryTokens = tokenize(normalizedQuery)
        if (queryTokens.isEmpty()) return true

        val wordTokens = haystacks.flatMap(::tokenize).distinct()

        return queryTokens.all { token ->
            tokenMatches(token, combined, wordTokens)
        }
    }

    private fun normalize(value: String): String =
        value.trim().lowercase().replace(Regex("\\s+"), " ")

    private fun tokenize(value: String): List<String> =
        value.split(Regex("[\\s\\-_,.]+")).filter { it.isNotEmpty() }

    private fun tokenMatches(token: String, combinedHaystack: String, words: List<String>): Boolean {
        if (combinedHaystack.contains(token)) return true

        if (words.any { word -> wordsOverlap(word, token) }) return true

        if (token.length >= 2 && isSubsequence(token, combinedHaystack.replace(" ", ""))) {
            return true
        }

        return false
    }

    private fun wordsOverlap(word: String, token: String): Boolean {
        if (word == token) return true
        if (word.contains(token) || token.contains(word)) return true
        if (word.startsWith(token) || token.startsWith(word)) return true
        return fuzzySimilar(word, token)
    }

    private fun fuzzySimilar(a: String, b: String): Boolean {
        if (a.isEmpty() || b.isEmpty()) return false
        val maxLen = maxOf(a.length, b.length)
        val allowedEdits = when {
            maxLen <= 3 -> 1
            maxLen <= 6 -> 2
            else -> maxLen / 4
        }
        return levenshteinDistance(a, b) <= allowedEdits
    }

    private fun isSubsequence(needle: String, haystack: String): Boolean {
        if (needle.isEmpty()) return true
        var index = 0
        for (char in haystack) {
            if (char == needle[index]) {
                index++
                if (index == needle.length) return true
            }
        }
        return false
    }

    private fun levenshteinDistance(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length

        val prev = IntArray(b.length + 1) { it }
        val curr = IntArray(b.length + 1)

        for (i in a.indices) {
            curr[0] = i + 1
            for (j in b.indices) {
                val cost = if (a[i] == b[j]) 0 else 1
                curr[j + 1] = minOf(
                    curr[j] + 1,
                    prev[j + 1] + 1,
                    prev[j] + cost
                )
            }
            for (j in prev.indices) prev[j] = curr[j]
        }
        return prev[b.length]
    }
}
