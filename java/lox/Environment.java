package lox;

import java.util.HashMap;
import java.util.Map;

class Environment {
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>(); // to store the [environmental] bindings

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        // Walking up the chain to find a variable, if there's none in this environment
        if (enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        // Similarly to get() above – looking up in the enclosing envirinment
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }

    // The constructor for the global scope's environment, which ends the chain
    Environment() {
        enclosing = null;
    }

    // The constructor for a local scope that is nested inside some outer one
    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    // value definition
    void define(String name, Object value) {
        values.put(name, value);
    }
}

