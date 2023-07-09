package lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    /*
    The start and current fields are offsets that index into the string.
    The start field points to the first character in the lexeme being scanned,
    and current points at the character currently being considered.
    The line field tracks what source line current is on,
    so we can produce tokens that know their location
     */

    private int start = 0;
    private int current = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("fun", TokenType.FUN);
        keywords.put("if", TokenType.IF);
        keywords.put("nil", TokenType.NIL);
        keywords.put("or", TokenType.OR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("var", TokenType.VAR);
        keywords.put("while", TokenType.WHILE);
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case '-': addToken(TokenType.MINUS); break;
            case '+': addToken(TokenType.PLUS); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '*': addToken(TokenType.STAR); break;
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;

            case '/':
                // we don't add either "comment tokens" nor comments themselves
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    // add support for nested comments?
                    while (!isAtEnd()) {
                        if (peek() == '\n') line++;
                        advance();
                    }
                    if (peek() != '*' && peekNext() != '/') {
                        advance();
                        advance();
                    } else {
                        // add error?
                    }
                } else {
                    addToken(TokenType.SLASH);
                }
                break;

            case ' ':
                // carriage return. moves cursor to the beginning of the line without advancing to new line
            case '\r':
                // tab
            case '\t':
                // Ignore whitespace.
                break;

            case '\n':
                line++;
                break;

            case '"': string(); break;
            case 'o':
                if (match('r')) {
                    addToken(TokenType.OR);
                }
                break;


            default:
                if (isDigit(c)) {
                    number();
                }
                // we assume any lexeme starting with _ or a letterr is an idenntifierr
                else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;

        }

    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = TokenType.IDENTIFIER;
        addToken(type);
    }

    //similar to strings
    private void number() {
        while (isDigit(peek())) advance();

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(TokenType.NUMBER,
                Double.parseDouble(source.substring(start, current)));
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        /*
        var counter = 1;
        System.out.println(counter++); // 1 (returned first then incremented)
        System.out.println(counter++); // 2
        var anotherCounter = 1;
        System.out.println(++counter); // 2 (incremented first then returned)
         */
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    // ≈ conditional advance()
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    // lookahead
    // making peek take a parameter would allow arbitrarily far lookaheads.
    // although it's possible to provide a boolean parameter?
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    // we need a second character of lookahead when workijng with decimal numbers
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    /*
    Java's Character.isDigit() allows Devangaeru digits,
    full-width (Chinese, JP, KR) numbers and other stuff we don't want
     */
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

}