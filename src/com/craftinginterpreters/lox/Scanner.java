package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.craftinginterpreters.lox.TokenType;


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


            default:
                Lox.error(line, "Unexpected character.");
                break;

        }

    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        /*
        var counter = 1;
        System.out.println(counter++);
        System.out.println(counter++);
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
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

}