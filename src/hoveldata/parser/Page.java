package hoveldata.parser;

public class Page {
    public Page prev, next;
    public Token read, write;

    public Page() {
        this(new Token());
    }

    public Page(Token t) {
        read = write = t;
    }

    public Page(Token r, Token w) {
        read = r;
        write = w;
    }

    public void append(Page p) {
        this.next = p;
        p.prev = this;
    }

    public void prepend(Page p) {
        if (prev != null) {
            prev.append(p);
        }
        p.append(this);
    }

    public void rewrite(Token t) {
        t.replace(write);
        write = t;
    }

    public void unread() {
        read.read = 0;
        while (read.prev != null) {
            read = read.prev;
            read.read = 0;
        }
    }

    public void add(Token t) {
        write.append(t);
        write = t;
    }

    public void insert(Token t) {
        read.prepend(t);
        read = t;
    }

    public int readChar() {
        int ch;
        while ((ch = read.readChar()) == -1) {
            Token t = read.next;
            if (t == null) {
                return -1;
            }
            read = t;
        }
        return ch;
    }

    public void nextToken() {
        read = read.next;
    }

    public int backupChar() {
        int ch;
        while ((ch = read.backupChar()) == -1) {
            Token t = read.prev;
            if (t == null) {
                return -1;
            }
            read = t;
        }
        return ch;
    }

    public boolean isMask() {
        return read.isMask();
    }

    public boolean isInvoked() {
        return read.isInvoked();
    }

    public Token.Kind getKind() {
        return read.getKind();
    }

    public void writeChar(int ch) {
        if (!write.writeChar(ch)) {
            add(new Token());
            write.writeChar(ch);
        }
    }

    public Page copy() {
        return new Page(read, write);
    }

    @Override
    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append(read);
        Token t = read;
        while ((t = t.next) != null) {
            b.append(t);
        }
        return b.toString();
    }

}
