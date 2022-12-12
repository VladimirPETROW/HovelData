package hoveldata.parser;

public class Token {
    public Token prev, next;
    char[] buffer;
    int read, write;
    boolean mask, invoked;

    Token.Kind kind;

    public Token() {
        this(2048);
    }

    public Token(int size) {
        buffer = new char[size];
    }

    public Token(String input) {
        buffer = input.toCharArray();
        write = buffer.length;
    }

    public Token(char[] b) {
        buffer = b;
        write = buffer.length;
    }

    public Token(char[] b, int r, int w) {
        buffer = b;
        read = r;
        write = w;
    }

    void append(Token t) {
        this.next = t;
        t.prev = this;
    }

    void replace(Token t) {
        prev = t.prev;
        next = t.next;
        if (prev != null) {
            prev.next = this;
        }
        if (next != null) {
            next.prev = this;
        }
    }

    void prepend(Token t) {
        if (prev != null) {
            prev.append(t);
        }
        t.append(this);
    }

    public int backupChar() {
        if (read == 0) return -1;
        read--;
        return buffer[read];
    }

    public int readChar() {
        if (read < write) {
            return buffer[read++];
        }
        return -1;
    }

    public void rtrim() {
        while (read < write) {
            int ch = buffer[write - 1];
            if ((ch != ' ') && (ch != '\t') && (ch != '\r') && (ch != '\n')) break;
            write--;
        }
    }

    public boolean writeChar(int ch) {
        if (write < buffer.length) {
            buffer[write++] = (char) ch;
            return true;
        }
        return false;
    }

    public int peekChar() {
        return buffer[read];
    }

    public int firstChar() {
        return buffer[0];
    }

    public void mask() {
        mask = true;
    }

    public boolean isMask() {
        return mask;
    }

    public void invoked() {
        invoked = true;
    }

    public boolean isInvoked() {
        return invoked;
    }

    public Token.Kind getKind() {
        return kind;
    }

    public int length() {
        return write - read;
    }

    public String details() {
        return this.getClass().getName() + ": kind = " + kind +
                ", buffer = " + new String(buffer, 0, write) +
                ", read = " + read + ", write = " + write + ", count = " + (write - read);
    }

    @Override
    public String toString() {
        return new String(buffer, read, write - read);
    }

    public enum Kind {
        SPACE,
        SYMBOL,
        LITERAL,
        TEXT,
        POINT,
        BIT,
        DATE,
        TIME,
        DATETIME,
        DIGIT,
        FRACTION,
        EXPONENT,
        POWER,
        HEX,
        DOT,
        WORD,
        EQUAL,
        PLUS,
        MINUS,
        AMPERSAND,
        PERCENT,
        SEMICOLON,
        UNEXPECTED,
        LET,
        MACRO,
        MEND
    }

}
