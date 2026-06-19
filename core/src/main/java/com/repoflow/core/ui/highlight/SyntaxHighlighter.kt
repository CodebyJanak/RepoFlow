package com.repoflow.core.ui.highlight

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

private val keywordColor = Color(0xFF7C3AED)
private val stringColor = Color(0xFF059669)
private val numberColor = Color(0xFFD97706)
private val commentColor = Color(0xFF6B7280)
private val annotationColor = Color(0xFF0891B2)
private val typeColor = Color(0xFF2563EB)
private val operatorColor = Color(0xFFDC2626)

private val kotlinConfig = SyntaxConfig(
    singleLineComment = "//",
    multiLineCommentStart = "/*",
    multiLineCommentEnd = "*/",
    keywords = setOf(
        "package", "import", "class", "object", "fun", "val", "var",
        "if", "else", "when", "for", "while", "do", "return",
        "try", "catch", "finally", "throw", "null", "true", "false",
        "this", "super", "is", "in", "as", "typealias", "data",
        "sealed", "abstract", "open", "override", "private", "protected",
        "public", "internal", "enum", "interface", "companion", "init",
        "constructor", "suspend", "inline", "infix", "operator", "tailrec",
        "external", "annotation", "inner", "value"
    ),
    types = setOf(
        "Int", "Long", "Float", "Double", "Boolean", "String", "Char",
        "Byte", "Short", "Unit", "Nothing", "Any", "List", "Map", "Set",
        "MutableList", "MutableMap", "MutableSet", "Array",
        "IntArray", "LongArray", "FloatArray", "DoubleArray", "BooleanArray",
        "CharArray", "ByteArray", "ShortArray"
    ),
    annotations = setOf(
        "Override", "Deprecated", "Suppress", "Inject", "Singleton",
        "Composable", "OptIn", "Experimental", "Immutable", "Stable",
        "JvmStatic", "JvmOverloads", "JvmField", "JvmName",
        "Parcelize", "Serializable", "Volatile", "Transient"
    )
)

private val javaConfig = SyntaxConfig(
    singleLineComment = "//",
    multiLineCommentStart = "/*",
    multiLineCommentEnd = "*/",
    keywords = setOf(
        "package", "import", "class", "interface", "enum", "extends",
        "implements", "abstract", "final", "static", "public", "private",
        "protected", "void", "return", "if", "else", "for", "while",
        "do", "switch", "case", "break", "continue", "new", "this",
        "super", "null", "true", "false", "try", "catch", "finally",
        "throw", "throws", "synchronized", "volatile", "transient",
        "native", "strictfp", "default", "var"
    ),
    types = setOf(
        "int", "long", "float", "double", "boolean", "char", "byte",
        "short", "String", "Integer", "Long", "Float", "Double",
        "Boolean", "Character", "Byte", "Short", "Void",
        "Object", "List", "Map", "Set", "Collection", "ArrayList",
        "HashMap", "HashSet", "Arrays", "Optional"
    ),
    annotations = setOf(
        "Override", "Deprecated", "SuppressWarnings", "FunctionalInterface",
        "SafeVarargs", "Generated", "Nullable", "NonNull"
    )
)

private val jsConfig = SyntaxConfig(
    singleLineComment = "//",
    multiLineCommentStart = "/*",
    multiLineCommentEnd = "*/",
    keywords = setOf(
        "import", "export", "default", "from", "class", "extends",
        "function", "const", "let", "var", "if", "else", "for", "while",
        "do", "switch", "case", "break", "continue", "return", "new",
        "this", "super", "null", "undefined", "true", "false",
        "try", "catch", "finally", "throw", "async", "await",
        "yield", "typeof", "instanceof", "in", "of", "static",
        "get", "set", "void", "delete"
    ),
    types = setOf(
        "String", "Number", "Boolean", "Object", "Array", "Function",
        "Map", "Set", "Promise", "RegExp", "Error", "Date",
        "Symbol", "BigInt", "JSON", "Math", "console"
    ),
    annotations = emptySet()
)

private val jsonSyntax = setOf(
    TokenMatcher.StringDQ to stringColor,
    TokenMatcher.Number to numberColor,
    TokenMatcher.Word(setOf("true", "false", "null")) to keywordColor
)

private val yamlSyntax = setOf(
    TokenMatcher.CommentHash to commentColor,
    TokenMatcher.Word(setOf("true", "false", "yes", "no", "on", "off", "null", "~")) to keywordColor,
    TokenMatcher.Number to numberColor,
    TokenMatcher.StringDQ to stringColor,
    TokenMatcher.StringSQ to stringColor
)

private val xmlConfig = XmlConfig(
    tagColor = Color(0xFF2563EB),
    attrColor = Color(0xFFD97706),
    stringColor = stringColor,
    commentColor = commentColor,
    valueColor = Color(0xFF7C3AED)
)

private data class SyntaxConfig(
    val singleLineComment: String,
    val multiLineCommentStart: String,
    val multiLineCommentEnd: String,
    val keywords: Set<String>,
    val types: Set<String>,
    val annotations: Set<String>
)

private data class XmlConfig(
    val tagColor: Color,
    val attrColor: Color,
    val stringColor: Color,
    val commentColor: Color,
    val valueColor: Color
)

private data class TokenMatch(val start: Int, val end: Int, val color: Color)

private sealed class TokenMatcher {
    data object StringDQ : TokenMatcher()
    data object StringSQ : TokenMatcher()
    data object Number : TokenMatcher()
    data object CommentHash : TokenMatcher()
    data class Word(val words: Set<String>) : TokenMatcher()
}

private val stringDQRegex = Regex("\"(?:[^\"\\\\]|\\\\.)*\"")
private val stringSQRegex = Regex("'(?:[^'\\\\]|\\\\.)*'")
private val numberRegex = Regex("\\b\\d+\\.?\\d*(?:[fFLl]|(?:\\.\\d+))?\\b")

private fun getSyntaxConfig(language: String): SyntaxConfig = when {
    language.equals("kt", true) || language.equals("kotlin", true) -> kotlinConfig
    language.equals("java", true) -> javaConfig
    language.equals("js", true) || language.equals("javascript", true) ||
        language.equals("ts", true) || language.equals("typescript", true) -> jsConfig
    else -> kotlinConfig
}

private fun getLanguageFromPath(filePath: String): String {
    val ext = filePath.substringAfterLast('.', "").lowercase()
    return when (ext) {
        "kt", "kts" -> "kotlin"
        "java" -> "java"
        "js", "mjs", "cjs" -> "js"
        "ts", "tsx" -> "js"
        "json" -> "json"
        "xml", "html", "svg", "xhtml" -> "xml"
        "yml", "yaml" -> "yaml"
        else -> ""
    }
}

fun detectLanguage(filePath: String): String = getLanguageFromPath(filePath)

fun highlightLine(line: String, language: String): AnnotatedString {
    if (line.isEmpty()) return AnnotatedString("")

    return when {
        language == "json" -> highlightWithMatchers(line, jsonSyntax)
        language == "yaml" || language == "yml" -> highlightWithMatchers(line, yamlSyntax)
        language == "xml" || language == "html" -> highlightXmlLine(line)
        language in setOf("kotlin", "java", "js") -> highlightCodeLine(line, getSyntaxConfig(language))
        else -> AnnotatedString(line)
    }
}

private fun highlightWithMatchers(line: String, matchers: Set<Pair<TokenMatcher, Color>>): AnnotatedString {
    val tokens = mutableListOf<TokenMatch>()
    val used = mutableSetOf<IntRange>()

    for ((matcher, color) in matchers) {
        val regex = when (matcher) {
            is TokenMatcher.StringDQ -> stringDQRegex
            is TokenMatcher.StringSQ -> stringSQRegex
            is TokenMatcher.Number -> numberRegex
            is TokenMatcher.CommentHash -> Regex("#.*")
            is TokenMatcher.Word -> {
                val pattern = matcher.words.joinToString("|") { Regex.escape(it) }
                Regex("\\b(?:$pattern)\\b")
            }
        }
        for (match in regex.findAll(line)) {
            val range = match.range
            if (used.none { it.intersect(range).any() }) {
                tokens.add(TokenMatch(range.first, range.last + 1, color))
                used.add(range)
            }
        }
    }

    tokens.sortBy { it.start }

    return buildAnnotatedString {
        var pos = 0
        for (token in tokens) {
            if (token.start > pos) append(line.substring(pos, token.start))
            withStyle(SpanStyle(color = token.color)) {
                append(line.substring(token.start, token.end))
            }
            pos = token.end
        }
        if (pos < line.length) append(line.substring(pos))
    }
}

private fun highlightXmlLine(line: String): AnnotatedString {
    return buildAnnotatedString {
        val comment = Regex("<!--.*?-->")
        val tag = Regex("</?\\w+[^>]*>")
        var pos = 0

        val allMatches = mutableListOf<Pair<IntRange, Color>>()
        for (m in comment.findAll(line)) {
            allMatches.add(m.range to commentColor)
        }
        for (m in tag.findAll(line)) {
            if (allMatches.none { it.first.intersect(m.range).any() }) {
                allMatches.add(m.range to xmlConfig.tagColor)
            }
        }

        val attr = Regex("\\b(\\w+)(\\s*=\\s*\"[^\"]*\")")
        for (m in attr.findAll(line)) {
            val range = m.range
            if (allMatches.none { it.first.intersect(range).any() }) {
                allMatches.add(range.first..(range.first + m.groupValues[1].length - 1) to xmlConfig.attrColor)
                val valueStart = range.first + m.groupValues[1].length
                allMatches.add(valueStart..range.last to xmlConfig.stringColor)
            }
        }

        allMatches.sortBy { it.first.first }

        for ((range, color) in allMatches) {
            if (range.first > pos) append(line.substring(pos, range.first))
            withStyle(SpanStyle(color = color)) {
                append(line.substring(range.first, range.last + 1))
            }
            pos = range.last + 1
        }
        if (pos < line.length) append(line.substring(pos))
    }
}

private fun highlightCodeLine(line: String, config: SyntaxConfig): AnnotatedString {
    val tokens = mutableListOf<TokenMatch>()
    val used = mutableSetOf<IntRange>()

    val singleLineIdx = if (config.singleLineComment.isNotEmpty()) {
        line.indexOf(config.singleLineComment)
    } else -1

    val commentPart = if (singleLineIdx >= 0) line.substring(singleLineIdx) else null
    val codePart = if (singleLineIdx >= 0) line.substring(0, singleLineIdx) else line

    if (commentPart != null) {
        val commentStart = singleLineIdx
        tokens.add(TokenMatch(commentStart, commentStart + commentPart.length, commentColor))
        used.add(commentStart until (commentStart + commentPart.length))
    }

    val combinedRegex = Regex("""(["'`])(?:(?!\1|\\).|\\.)*\1|""" +
        """\b\d+\.?\d*(?:[fFLl]|(?:\s*\.\s*\d+))?\b|""" +
        """@\w+|""" +
        """\b\w+\b""")

    for (match in combinedRegex.findAll(codePart)) {
        val range = match.range
        if (used.none { it.intersect(range).any() }) {
            val text = match.value
            val color = when {
                text.startsWith("\"") || text.startsWith("'") || text.startsWith("`") -> stringColor
                text.startsWith("@") -> annotationColor
                text.first().isDigit() -> numberColor
                text in config.keywords -> keywordColor
                text in config.types -> typeColor
                text in config.annotations -> annotationColor
                else -> null
            }
            if (color != null) {
                tokens.add(TokenMatch(range.first, range.last + 1, color))
                used.add(range)
            }
        }
    }

    tokens.sortBy { it.start }

    return buildAnnotatedString {
        var pos = 0
        for (token in tokens) {
            if (token.start > pos) append(line.substring(pos, token.start))
            withStyle(SpanStyle(color = token.color)) {
                append(line.substring(token.start, token.end))
            }
            pos = token.end
        }
        if (pos < line.length) append(line.substring(pos))
    }
}
