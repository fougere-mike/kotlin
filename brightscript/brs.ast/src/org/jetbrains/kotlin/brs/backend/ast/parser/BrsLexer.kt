/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.brs.backend.ast.parser

/**
 * Token types for BrightScript lexical analysis.
 */
enum class BrsTokenType {
    // Literals
    INTEGER_LITERAL,
    LONG_INTEGER_LITERAL,
    FLOAT_LITERAL,
    DOUBLE_LITERAL,
    STRING_LITERAL,

    // Identifiers
    IDENTIFIER,

    // Keywords
    FUNCTION,
    SUB,
    END,
    IF,
    THEN,
    ELSE,
    ELSE_IF,
    FOR,
    TO,
    STEP,
    EACH,
    IN,
    WHILE,
    RETURN,
    PRINT,
    DIM,
    AS,
    AND,
    OR,
    NOT,
    MOD,
    TRUE,
    FALSE,
    INVALID,
    TRY,
    CATCH,
    THROW,
    EXIT,
    CONTINUE,
    STOP,
    GOTO,
    LIBRARY,
    NEXT,
    REM,

    // Type keywords
    TYPE_INTEGER,
    TYPE_LONGINTEGER,
    TYPE_FLOAT,
    TYPE_DOUBLE,
    TYPE_STRING,
    TYPE_BOOLEAN,
    TYPE_OBJECT,
    TYPE_DYNAMIC,
    TYPE_VOID,
    TYPE_FUNCTION,

    // Operators
    PLUS,           // +
    MINUS,          // -
    STAR,           // *
    SLASH,          // /
    BACKSLASH,      // \
    CARET,          // ^
    EQ,             // =
    NE,             // <>
    LT,             // <
    GT,             // >
    LE,             // <=
    GE,             // >=
    LEFT_SHIFT,     // <<
    RIGHT_SHIFT,    // >>

    // Punctuation
    LPAREN,         // (
    RPAREN,         // )
    LBRACKET,       // [
    RBRACKET,       // ]
    LBRACE,         // {
    RBRACE,         // }
    DOT,            // .
    COMMA,          // ,
    COLON,          // :
    SEMICOLON,      // ;
    AT,             // @
    QUESTION,       // ?

    // Special
    NEWLINE,
    COMMENT,
    EOF,

    // Error
    ERROR
}

/**
 * A token produced by the BrightScript lexer.
 */
data class BrsToken(
    val type: BrsTokenType,
    val text: String,
    val line: Int,
    val column: Int,
    val value: Any? = null  // Parsed value for literals
)

/**
 * Lexer (tokenizer) for BrightScript source code.
 *
 * Converts BrightScript source text into a stream of tokens
 * that can be consumed by the parser.
 */
class BrsLexer(private val source: String) {

    private var position = 0
    private var line = 1
    private var column = 1
    private var tokenStart = 0
    private var tokenStartLine = 1
    private var tokenStartColumn = 1

    private val tokens = mutableListOf<BrsToken>()

    companion object {
        private val KEYWORDS = mapOf(
            "function" to BrsTokenType.FUNCTION,
            "sub" to BrsTokenType.SUB,
            "end" to BrsTokenType.END,
            "if" to BrsTokenType.IF,
            "then" to BrsTokenType.THEN,
            "else" to BrsTokenType.ELSE,
            "elseif" to BrsTokenType.ELSE_IF,
            "for" to BrsTokenType.FOR,
            "to" to BrsTokenType.TO,
            "step" to BrsTokenType.STEP,
            "each" to BrsTokenType.EACH,
            "in" to BrsTokenType.IN,
            "while" to BrsTokenType.WHILE,
            "return" to BrsTokenType.RETURN,
            "print" to BrsTokenType.PRINT,
            "dim" to BrsTokenType.DIM,
            "as" to BrsTokenType.AS,
            "and" to BrsTokenType.AND,
            "or" to BrsTokenType.OR,
            "not" to BrsTokenType.NOT,
            "mod" to BrsTokenType.MOD,
            "true" to BrsTokenType.TRUE,
            "false" to BrsTokenType.FALSE,
            "invalid" to BrsTokenType.INVALID,
            "try" to BrsTokenType.TRY,
            "catch" to BrsTokenType.CATCH,
            "throw" to BrsTokenType.THROW,
            "exit" to BrsTokenType.EXIT,
            "continue" to BrsTokenType.CONTINUE,
            "stop" to BrsTokenType.STOP,
            "goto" to BrsTokenType.GOTO,
            "library" to BrsTokenType.LIBRARY,
            "next" to BrsTokenType.NEXT,
            "rem" to BrsTokenType.REM,
            // Type keywords
            "integer" to BrsTokenType.TYPE_INTEGER,
            "longinteger" to BrsTokenType.TYPE_LONGINTEGER,
            "float" to BrsTokenType.TYPE_FLOAT,
            "double" to BrsTokenType.TYPE_DOUBLE,
            "string" to BrsTokenType.TYPE_STRING,
            "boolean" to BrsTokenType.TYPE_BOOLEAN,
            "object" to BrsTokenType.TYPE_OBJECT,
            "dynamic" to BrsTokenType.TYPE_DYNAMIC,
            "void" to BrsTokenType.TYPE_VOID
        )
    }

    /**
     * Tokenize the entire source and return the list of tokens.
     */
    fun tokenize(): List<BrsToken> {
        tokens.clear()
        position = 0
        line = 1
        column = 1

        while (!isAtEnd()) {
            tokenStart = position
            tokenStartLine = line
            tokenStartColumn = column
            scanToken()
        }

        tokens.add(BrsToken(BrsTokenType.EOF, "", line, column))
        return tokens
    }

    private fun isAtEnd(): Boolean = position >= source.length

    private fun peek(): Char = if (isAtEnd()) '\u0000' else source[position]

    private fun peekNext(): Char = if (position + 1 >= source.length) '\u0000' else source[position + 1]

    private fun advance(): Char {
        val c = source[position++]
        if (c == '\n') {
            line++
            column = 1
        } else {
            column++
        }
        return c
    }

    private fun match(expected: Char): Boolean {
        if (isAtEnd() || source[position] != expected) return false
        advance()
        return true
    }

    private fun addToken(type: BrsTokenType, value: Any? = null) {
        val text = source.substring(tokenStart, position)
        tokens.add(BrsToken(type, text, tokenStartLine, tokenStartColumn, value))
    }

    private fun scanToken() {
        when (val c = advance()) {
            // Whitespace (not newline)
            ' ', '\t', '\r' -> { /* skip */ }

            // Newline (significant in BrightScript)
            '\n' -> addToken(BrsTokenType.NEWLINE)

            // Single-character tokens
            '(' -> addToken(BrsTokenType.LPAREN)
            ')' -> addToken(BrsTokenType.RPAREN)
            '[' -> addToken(BrsTokenType.LBRACKET)
            ']' -> addToken(BrsTokenType.RBRACKET)
            '{' -> addToken(BrsTokenType.LBRACE)
            '}' -> addToken(BrsTokenType.RBRACE)
            '.' -> addToken(BrsTokenType.DOT)
            ',' -> addToken(BrsTokenType.COMMA)
            ':' -> addToken(BrsTokenType.COLON)
            ';' -> addToken(BrsTokenType.SEMICOLON)
            '@' -> addToken(BrsTokenType.AT)
            '?' -> addToken(BrsTokenType.QUESTION)
            '+' -> addToken(BrsTokenType.PLUS)
            '-' -> addToken(BrsTokenType.MINUS)
            '*' -> addToken(BrsTokenType.STAR)
            '/' -> addToken(BrsTokenType.SLASH)
            '\\' -> addToken(BrsTokenType.BACKSLASH)
            '^' -> addToken(BrsTokenType.CARET)
            '=' -> addToken(BrsTokenType.EQ)

            // Two-character tokens
            '<' -> {
                when {
                    match('>') -> addToken(BrsTokenType.NE)
                    match('=') -> addToken(BrsTokenType.LE)
                    match('<') -> addToken(BrsTokenType.LEFT_SHIFT)
                    else -> addToken(BrsTokenType.LT)
                }
            }
            '>' -> {
                when {
                    match('=') -> addToken(BrsTokenType.GE)
                    match('>') -> addToken(BrsTokenType.RIGHT_SHIFT)
                    else -> addToken(BrsTokenType.GT)
                }
            }

            // Comment
            '\'' -> scanComment()

            // String literal
            '"' -> scanString()

            else -> {
                when {
                    c.isDigit() -> scanNumber()
                    c.isLetter() || c == '_' -> scanIdentifierOrKeyword()
                    else -> addToken(BrsTokenType.ERROR, "Unexpected character: $c")
                }
            }
        }
    }

    private fun scanComment() {
        // Comment goes until end of line
        val start = position
        while (!isAtEnd() && peek() != '\n') {
            advance()
        }
        val commentText = source.substring(start, position)
        addToken(BrsTokenType.COMMENT, commentText)
    }

    private fun scanString() {
        val builder = StringBuilder()

        while (!isAtEnd() && peek() != '"') {
            val c = advance()
            if (c == '\n') {
                addToken(BrsTokenType.ERROR, "Unterminated string")
                return
            }
            builder.append(c)
        }

        if (isAtEnd()) {
            addToken(BrsTokenType.ERROR, "Unterminated string")
            return
        }

        // Consume closing quote
        advance()

        // Check for adjacent string (BrightScript string concatenation via quotes)
        // e.g., "Hello" "World" -> "HelloWorld"
        while (!isAtEnd() && peek() == '"') {
            advance() // consume opening quote
            while (!isAtEnd() && peek() != '"') {
                val c = advance()
                if (c == '\n') {
                    addToken(BrsTokenType.ERROR, "Unterminated string")
                    return
                }
                builder.append(c)
            }
            if (!isAtEnd()) {
                advance() // consume closing quote
            }
        }

        addToken(BrsTokenType.STRING_LITERAL, builder.toString())
    }

    private fun scanNumber() {
        // Scan integer part
        while (!isAtEnd() && peek().isDigit()) {
            advance()
        }

        // Check for type suffix or decimal point
        when {
            // Long integer suffix
            peek() == '&' -> {
                advance()
                val text = source.substring(tokenStart, position - 1)
                addToken(BrsTokenType.LONG_INTEGER_LITERAL, text.toLongOrNull() ?: 0L)
            }
            // Float suffix
            peek() == '!' -> {
                advance()
                val text = source.substring(tokenStart, position - 1)
                addToken(BrsTokenType.FLOAT_LITERAL, text.toFloatOrNull() ?: 0f)
            }
            // Double suffix
            peek() == '#' -> {
                advance()
                val text = source.substring(tokenStart, position - 1)
                addToken(BrsTokenType.DOUBLE_LITERAL, text.toDoubleOrNull() ?: 0.0)
            }
            // Decimal point (float or double)
            peek() == '.' && peekNext().isDigit() -> {
                advance() // consume '.'
                while (!isAtEnd() && peek().isDigit()) {
                    advance()
                }
                // Check for exponent
                if (peek().lowercaseChar() == 'e') {
                    advance()
                    if (peek() == '+' || peek() == '-') advance()
                    while (!isAtEnd() && peek().isDigit()) {
                        advance()
                    }
                }
                // Check for type suffix
                when (peek()) {
                    '!' -> {
                        advance()
                        val text = source.substring(tokenStart, position - 1)
                        addToken(BrsTokenType.FLOAT_LITERAL, text.toFloatOrNull() ?: 0f)
                    }
                    '#' -> {
                        advance()
                        val text = source.substring(tokenStart, position - 1)
                        addToken(BrsTokenType.DOUBLE_LITERAL, text.toDoubleOrNull() ?: 0.0)
                    }
                    else -> {
                        // Default to float for decimal numbers in BrightScript
                        val text = source.substring(tokenStart, position)
                        addToken(BrsTokenType.FLOAT_LITERAL, text.toFloatOrNull() ?: 0f)
                    }
                }
            }
            // Exponent without decimal point
            peek().lowercaseChar() == 'e' -> {
                advance()
                if (peek() == '+' || peek() == '-') advance()
                while (!isAtEnd() && peek().isDigit()) {
                    advance()
                }
                val text = source.substring(tokenStart, position)
                addToken(BrsTokenType.DOUBLE_LITERAL, text.toDoubleOrNull() ?: 0.0)
            }
            // Plain integer
            else -> {
                val text = source.substring(tokenStart, position)
                addToken(BrsTokenType.INTEGER_LITERAL, text.toIntOrNull() ?: 0)
            }
        }
    }

    private fun scanIdentifierOrKeyword() {
        while (!isAtEnd() && (peek().isLetterOrDigit() || peek() == '_')) {
            advance()
        }

        // Check for type suffix on identifiers (e.g., myVar$ for string, myVar% for integer)
        when (peek()) {
            '$', '%', '!', '#', '&' -> advance()
        }

        val text = source.substring(tokenStart, position)
        val lowerText = text.lowercase()

        // Check for REM comment
        if (lowerText == "rem") {
            // REM comment - rest of line is comment
            while (!isAtEnd() && peek() != '\n') {
                advance()
            }
            val commentText = source.substring(tokenStart + 3, position).trimStart()
            addToken(BrsTokenType.COMMENT, commentText)
            return
        }

        // Check for keyword
        val type = KEYWORDS[lowerText] ?: BrsTokenType.IDENTIFIER
        addToken(type)
    }
}

/**
 * Parse result containing tokens and any lexer errors.
 */
data class BrsLexResult(
    val tokens: List<BrsToken>,
    val errors: List<String>
) {
    val hasErrors: Boolean get() = errors.isNotEmpty()

    companion object {
        fun fromTokens(tokens: List<BrsToken>): BrsLexResult {
            val errors = tokens
                .filter { it.type == BrsTokenType.ERROR }
                .map { "Line ${it.line}:${it.column}: ${it.value}" }
            return BrsLexResult(tokens, errors)
        }
    }
}

/**
 * Tokenize BrightScript source code.
 */
fun tokenizeBrightScript(source: String): BrsLexResult {
    val lexer = BrsLexer(source)
    val tokens = lexer.tokenize()
    return BrsLexResult.fromTokens(tokens)
}
