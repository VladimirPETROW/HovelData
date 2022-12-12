package hoveldata.parser;

import java.util.HashMap;

public class Resolver {
    public HashMap<String, String> values;
    Page read, write;

    Token level, name;
    private boolean resolve;

    public Resolver(HashMap<String, String> v, Token l) {
        values = v;
        read = new Page(l);
    }

    public void setValues(HashMap<String, String> v) {
        values = v;
    }

    public int readChar() {
        int ch;
        while ((ch = nextChar()) != -1) {
            if ((ch == '&') && (!isMask())) {
                write = new Page();
                write.writeChar(ch);
                level = write.write;
                name = null;
                resolve = true;
                perform();
                backup(write);
                continue;
            }
            break;
        }
        return ch;
    }

    public int nextChar() {
        int ch;
        while ((ch = read.readChar()) == -1) {
            Page r = read.prev;
            if (r == null) {
                return -1;
            }
            read = r;
        }
        return ch;
    }

    public int backupChar() {
        int ch;
        while ((ch = read.backupChar()) == -1) {
            Page r = read.next;
            if (r == null) {
                return -1;
            }
            read = r;
        }
        return ch;
    }

    public void nextToken() {
        read.nextToken();
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

    public void backup(Page p) {
        read.append(p);
        read = p;
    }

    public void perform() {
        int ch;
        while ((ch = nextChar()) != -1) {
            if ((ch == '&') && (!isMask())) {
                if (level == null) {
                    write.add(new Token());
                    write.writeChar(ch);
                    level = write.write;
                    resolve = true;
                    continue;
                }
                if (name == null) {
                    resolve = !resolve;
                    if (resolve) {
                        write.writeChar(ch);
                    }
                    continue;
                }
                resolve(!Character.isDigit(name.peekChar()));
                write.add(new Token());
                write.writeChar(ch);
                level = write.write;
                name = null;
                resolve = true;
                continue;
            }
            if ((ch == '_') || Character.isAlphabetic(ch) || Character.isDigit(ch)) {
                if (level == null) {
                    write.writeChar(ch);
                    continue;
                }
                if (name == null) {
                    name = new Token(32);
                    write.add(name);
                }
                name.writeChar(ch);
                continue;
            }
            if (ch == '.') {
                if (level == null) {
                    backupChar();
                    return;
                }
                if (!resolve((name != null) && !Character.isDigit(name.peekChar()))) {
                    write.writeChar(ch);
                }
                level = null;
                name = null;
                resolve = false;
                continue;
            }
            backupChar();
            break;
        }
        if (level != null) {
            resolve((name != null) && !Character.isDigit(name.peekChar()));
        }
    }

    boolean resolve(boolean check) {
        if (!check) {
            while (level.length() > 1) {
                level.readChar();
            }
            level.mask();
            return false;
        }
        if (resolve) {
            String key = name.toString();
            String value = values.get(key);
            if (value != null) {
                level.readChar();
                write.rewrite(new Token(value));
                return true;
            }
            if (level.length() == 1) {
                level.mask();
                System.out.println("WARNING: Apparent symbolic reference " + key.toUpperCase() + " not resolved.");
            }
        }
        return false;
    }

}
