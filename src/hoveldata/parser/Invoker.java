package hoveldata.parser;

import java.util.*;

public class Invoker {

    public HashMap<String, String> macroses;
    Page write;
    Resolver resolver;

    Token percent, name, param;

    HashMap<String, Token.Kind> statement;

    public Invoker(HashMap<String, String> m, Resolver r) {
        statement = new HashMap<String, Token.Kind>();
        statement.put("LET", Token.Kind.LET);
        statement.put("MACRO", Token.Kind.MACRO);
        statement.put("MEND", Token.Kind.MEND);

        macroses = m;
        resolver = r;
    }

    public int readChar() {
        int ch, nch;
        while ((ch = resolver.readChar()) != -1) {
            if ((ch == '%') && !isMask() && !isInvoked()) {
                percent = new Token(new char[]{(char) ch});
                Page p = new Page(percent);
                if (write != null) write.append(p);
                write = p;
                name = new Token();
                param = null;
                nch = resolver.readChar();
                if (((nch < 'A') || (nch > 'Z')) && ((nch < 'a') || (nch > 'z')) && (nch != '_')) {
                    percent.mask();
                    flush();
                    continue;
                }
                write.add(name);
                name.writeChar(nch);
                while ((nch = readChar()) != -1) {
                    if (((nch < '0') || (nch > '9')) && ((nch < 'A') || (nch > 'Z')) && ((nch < 'a') || (nch > 'z')) && (nch != '_'))
                        break;
                    name.writeChar(nch);
                }
                Token space = new Token();
                while ((nch == ' ') || (nch == '\t') || (nch == '\r') || (nch == '\n')) {
                    space.writeChar(nch);
                    nch = resolver.readChar();
                }
                if (nch != '(') {
                    if (nch != -1) resolver.backupChar();
                    perform();
                    percent = null;
                    name = null;
                    write.add(space);
                    flush();
                    continue;
                }
                write.add(space);
                write.writeChar(nch);
                nch = resolver.readChar();
                while ((nch == ' ') || (nch == '\t') || (nch == '\r') || (nch == '\n')) {
                    write.writeChar(nch);
                    nch = resolver.readChar();
                }
                if (nch != -1) resolver.backupChar();
                param = new Token();
                param.mask();
                write.add(param);
                continue;
            }
            if (param != null) {
                if (ch == '\'') {
                    param.writeChar(ch);
                    while ((nch = resolver.nextChar()) != -1) {
                        param.writeChar(nch);
                        if (nch == '\'') break;
                    }
                    continue;
                }
                if (ch == '"') {
                    param.writeChar(ch);
                    while ((nch = resolver.nextChar()) != -1) {
                        param.writeChar(nch);
                        if (nch == '"') break;
                    }
                    continue;
                }
                if (ch == ',') {
                    Token delim = new Token();
                    write.add(delim);
                    write.writeChar(ch);
                    nch = resolver.readChar();
                    while ((nch == ' ') || (nch == '\t') || (nch == '\r') || (nch == '\n')) {
                        write.writeChar(nch);
                        nch = resolver.readChar();
                    }
                    if (nch != -1) resolver.backupChar();
                    param = new Token();
                    param.mask();
                    write.add(param);
                    continue;
                }
                if (ch == ')') {
                    perform();
                    percent = null;
                    name = null;
                    param = null;
                    flush();
                    continue;
                }
                param.writeChar(ch);
                continue;
            }
            break;
        }
        return ch;
    }

    public int nextChar() {
        return resolver.nextChar();
    }

    public void backupChar() {
        resolver.backupChar();
    }

    public void nextToken() {
        resolver.nextToken();
    }

    public boolean isMask() {
        return resolver.isMask();
    }

    public boolean isInvoked() {
        return resolver.isInvoked();
    }

    public Token.Kind getKind() {
        return resolver.getKind();
    }

    public void perform() {
        String key = name.toString();
        Token.Kind kind = statement.get(key.toUpperCase());
        if (kind != null) {
            percent.invoked();
            name.kind = kind;
            return;
        }
        String macros = macroses.get(key);
        if (macros == null) {
            System.out.println("WARNING: Apparent invocation of macro " + key.toUpperCase() + " not resolved.");
            percent.invoked();
            return;
        }
        Token t = new Token(macros);
        if (write.prev == null) {
            write = new Page(t);
        }
        else {
            write = write.prev;
            write.next = null;
            write.add(t);
        }
    }

    void flush() {
        Page c = write.copy();
        resolver.backup(c);
        while (write.prev != null) {
            write = write.prev;
            write.unread();
            c = write.copy();
            resolver.backup(c);
        }
        write = null;
    }

}
