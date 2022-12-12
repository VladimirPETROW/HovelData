package hoveldata.parser;

public class Tokenizer {

    Setter setter;
    public Token create;
    boolean resolve;

    Token.Kind expect;

    public Tokenizer(Setter s) {
        setter = s;
        resolve = true;
    }

    public Token getToken() {
        Token token = create;
        create = null;
        if (token == null) {
            token = new Token();
        }
        int ch, nch;
        while ((ch = readChar()) != -1) {
            if (expect == Token.Kind.DOT) {
                if (ch == '.') {
                    token.writeChar(ch);
                    token.kind = Token.Kind.DOT;
                    expect = Token.Kind.FRACTION;
                    return token;
                }
                if (((ch >= 'A') && (ch <= 'F')) || ((ch >= 'a') && (ch <= 'f'))) {
                    token.writeChar(ch);
                    while ((nch = readChar()) != -1) {
                        if (((nch < '0') || (nch > '9')) && ((nch < 'A') || (nch > 'F')) && ((nch < 'a') || (nch > 'f'))) {
                            backupChar();
                            break;
                        }
                        token.writeChar(nch);
                    }
                    if ((token.write == 1) && ((ch == 'E') || (ch == 'e'))) {
                        token.kind = Token.Kind.EXPONENT;
                        expect = Token.Kind.POWER;
                        return token;
                    }
                    if (nch == 'x') {
                        token.kind = Token.Kind.HEX;
                        expect = null;
                        return token;
                    }
                    while ((nch = readChar()) != -1) {
                        if (((nch < '0') || (nch > '9')) && ((nch < 'A') || (nch > 'Z')) && ((nch < 'a') || (nch > 'z')) && (nch != '_')) {
                            backupChar();
                            break;
                        }
                        token.writeChar(nch);
                    }
                    token.kind = Token.Kind.WORD;
                    expect = null;
                    return token;
                }
                expect = null;
            }
            if (expect == Token.Kind.FRACTION) {
                if ((ch >= '0') && (ch <= '9')) {
                    token.writeChar(ch);
                    while ((nch = readChar()) != -1) {
                        if ((nch < '0') || (nch > '9')) {
                            backupChar();
                            break;
                        }
                        token.writeChar(nch);
                    }
                    token.kind = Token.Kind.FRACTION;
                    expect = Token.Kind.EXPONENT;
                    return token;
                }
                if ((ch == 'E') || (ch == 'e')) {
                    token.writeChar(ch);
                    token.kind = Token.Kind.EXPONENT;
                    expect = null;
                    return token;
                }
                expect = null;
            }
            if (expect == Token.Kind.EXPONENT) {
                if ((ch == 'E') || (ch == 'e')) {
                    token.writeChar(ch);
                    token.kind = Token.Kind.EXPONENT;
                    expect = Token.Kind.POWER;
                    return token;
                }
                expect = null;
            }
            if (expect == Token.Kind.POWER) {
                if (ch == '+') {
                    token.writeChar(ch);
                    token.kind = Token.Kind.PLUS;
                    create = null;
                    return token;
                }
                if (ch == '-') {
                    token.writeChar(ch);
                    token.kind = Token.Kind.MINUS;
                    return token;
                }
                if ((ch >= '0') && (ch <= '9')) {
                    token.writeChar(ch);
                    while ((nch = readChar()) != -1) {
                        if ((nch < '0') || (nch > '9')) {
                            backupChar();
                            break;
                        }
                        token.writeChar(nch);
                    }
                    token.kind = Token.Kind.POWER;
                    expect = null;
                    return token;
                }
                expect = null;
            }
            if ((ch == ' ') || (ch == '\t') || (ch == '\r') || (ch == '\n')) {
                token.writeChar(ch);
                while ((nch = readChar()) != -1) {
                    if ((nch != ' ') && (nch != '\t') && (nch != '\r') && (nch != '\n')) {
                        backupChar();
                        break;
                    }
                    token.writeChar(nch);
                }
                token.kind = Token.Kind.SPACE;
                return token;
            }
            if (((ch >= 'A') && (ch <= 'Z')) || ((ch >= 'a') && (ch <= 'z')) || (ch == '_')) {
                token.writeChar(ch);
                while ((nch = readChar()) != -1) {
                    if (((nch < '0') || (nch > '9')) && ((nch < 'A') || (nch > 'Z')) && ((nch < 'a') || (nch > 'z')) && (nch != '_')) {
                        backupChar();
                        break;
                    }
                    token.writeChar(nch);
                }
                token.kind = Token.Kind.WORD;
                return token;
            }
            if (ch == '\'') {
                token.kind = Token.Kind.LITERAL;
                resolve = false;
                while ((ch = readChar()) != -1) {
                    if (ch == '\'') {
                        resolve = true;
                        nch = readChar();
                        if (nch == ch) {
                            token.writeChar(ch);
                            resolve = false;
                            continue;
                        }
                        return endLiteral(token, nch);
                    }
                    token.writeChar(ch);
                }
                break;
            }
            if (ch == '"') {
                token.kind = Token.Kind.TEXT;
                while ((ch = readChar()) != -1) {
                    if (ch == '"') {
                        nch = readChar();
                        if (nch == ch) {
                            token.writeChar(ch);
                            continue;
                        }
                        return endLiteral(token, nch);
                    }
                    token.writeChar(ch);
                }
                break;
            }
            if ((ch >= '0') && (ch <= '9')) {
                token.writeChar(ch);
                while ((nch = readChar()) != -1) {
                    if ((nch < '0') || (nch > '9')) {
                        backupChar();
                        break;
                    }
                    token.writeChar(nch);
                }
                token.kind = Token.Kind.DIGIT;
                expect = Token.Kind.DOT;
                return token;
            }
            token.writeChar(ch);
            token.kind = Token.Kind.SYMBOL;
            return token;
        }
        if (token.write > 0) {
            create = token;
        }
        return null;
    }

    Token endLiteral(Token token, int nch) {
        if (nch == 'b') {
            token.kind = Token.Kind.BIT;
            return token;
        }
        if (nch == 'd') {
            nch = readChar();
            if (nch == 't') {
                token.kind = Token.Kind.DATETIME;
                return token;
            }
            backupChar();
            token.kind = Token.Kind.DATE;
            return token;
        }
        if (nch == 'n') {
            token.kind = Token.Kind.WORD;
            return token;
        }
        if (nch == 't') {
            token.kind = Token.Kind.TIME;
            return token;
        }
        if (nch == 'x') {
            token.kind = Token.Kind.POINT;
            return token;
        }
        backupChar();
        return token;
    }

    public int readChar() {
        if (resolve) {
            return setter.readChar();
        }
        return setter.nextChar();
    }

    public void backupChar() {
        setter.backupChar();
    }

    public boolean isMask() {
        return setter.isMask();
    }

}
