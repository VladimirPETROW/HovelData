package hoveldata.parser;

import java.util.HashMap;

public class Setter {

    public HashMap<String, String> val;
    Page write;
    Invoker invoker;

    Token percent, name, value;

    public Setter(HashMap<String, String> v, Invoker i) {
        val = v;
        invoker = i;
    }

    public int readChar() {
        int ch, nch;
        while ((ch = invoker.readChar()) != -1) {
            if ((ch == '%') && (!isMask())) {
                nch = invoker.readChar();
                Token.Kind kind = invoker.getKind();
                if (kind == Token.Kind.LET) {
                    invoker.nextToken();
                    nch = invoker.readChar();
                    while ((nch == ' ') || (nch == '\t') || (nch == '\r') || (nch == '\n')) {
                        nch = invoker.readChar();
                    }
                    if (((nch < 'A') || (nch > 'Z')) && ((nch < 'a') || (nch > 'z')) && (nch != '_')) {
                        System.out.println("ERROR: Expecting a variable name after %LET.");
                        do {
                            if (nch == ';') break;
                        }
                        while ((nch = invoker.readChar()) != -1);
                        continue;
                    }
                    name = new Token();
                    name.writeChar(nch);
                    while ((nch = readChar()) != -1) {
                        if (((nch < '0') || (nch > '9')) && ((nch < 'A') || (nch > 'Z')) && ((nch < 'a') || (nch > 'z')) && (nch != '_')) break;
                        name.writeChar(nch);
                    }
                    Token wrong = new Token();
                    do {
                        if ((nch == ' ') || (nch == '\t') || (nch == '\r') || (nch == '\n') || (nch == '=') || (nch == ';')) break;
                        wrong.writeChar(nch);
                    }
                    while ((nch = invoker.readChar()) != -1);
                    if (wrong.length() > 0) {
                        System.out.println("ERROR: Symbolic variable name " + name.toString().toUpperCase() + wrong.toString().toUpperCase() + " must contain only letters, digits, and underscores.");
                        do {
                            if (nch == ';') break;
                        }
                        while ((nch = invoker.readChar()) != -1);
                        continue;
                    }
                    while ((nch == ' ') || (nch == '\t') || (nch == '\r') || (nch == '\n')) {
                        nch = invoker.readChar();
                    }
                    if (nch != '=') {
                        System.out.println("NOTE: A missing equal sign has been inserted after the variable name " + name.toString().toUpperCase() + ".");
                    }
                    else {
                        nch = invoker.readChar();
                        while ((nch == ' ') || (nch == '\t') || (nch == '\r') || (nch == '\n')) {
                            nch = invoker.readChar();
                        }
                    }
                    value = new Token();
                    do {
                        if (nch == ';') break;
                        value.writeChar(nch);
                    }
                    while ((nch = invoker.readChar()) != -1);
                    value.rtrim();
                    val.put(name.toString(), value.toString());
                    continue;
                }
                invoker.backupChar();
                break;
            }
            break;
        }
        return ch;
    }

    public int nextChar() {
        return invoker.nextChar();
    }

    public void backupChar() {
        invoker.backupChar();
    }

    public boolean isMask() {
        return invoker.isMask();
    }

}
