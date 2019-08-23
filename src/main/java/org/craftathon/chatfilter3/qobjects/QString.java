package org.craftathon.chatfilter3.qobjects;

import org.craftathon.chatfilter3.main.ChatFilter;

import java.util.ArrayList;
import java.util.List;

public class QString {

    private ChatFilter chatFilter;
    private List<QChar> qChars = new ArrayList<>();
    private int length;

    private QString() {}

    public QString(ChatFilter chatFilter, String value) {
        this.chatFilter = chatFilter;
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            qChars.add(chatFilter.getQCharFor(chars[i]).setOriginalChar(chars[i]).setIndex(i));
        }

        this.length = chars.length;
    }

    public boolean equals(QString qString) {
        return qString.qChars.equals(this.qChars);
    }

    public QString stripRepeating() {
        List<QChar> temp = new ArrayList<>();

        QChar last = null;

        for (QChar qChar : qChars) {
            if (last != null) {
                if (last.equalsIgnoreCase(qChar)) {
                    last.addRepetition(qChar.getOriginalChar(0));
                } else {
                    last = qChar;
                    temp.add(qChar);
                }
            } else {
                last = qChar;
                temp.add(qChar);
            }
        }

        this.qChars = temp;
        return this;
    }

    public int getRealLength() {
        return this.length;
    }

    public int getIteratingLength() {
        return this.qChars.size();
    }

    public QChar qCharAt(int index) {
        return this.qChars.size() > index && index >= 0 ? this.qChars.get(index) : null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        qChars.forEach(qChar -> sb.append(qChar.toString()));
        return sb.toString() + "\n";
    }

    public String reconstruct() {
        StringBuilder stringBuilder = new StringBuilder();

        for (QChar qChar : this.qChars) {
            if (!qChar.isPlaceholder()) {
                for (int i = 0; i < qChar.getRepetition(); i++) {
                    stringBuilder.append(qChar.getOriginalChar(i));
                }
            }
        }

        return stringBuilder.toString();
    }

    public String reconstructFrom(int startIndex) {
        StringBuilder stringBuilder = new StringBuilder();

        for (QChar qChar : this.qChars) {
            if (startIndex <= 0) {
                if (!qChar.isPlaceholder()) {
                    for (int i = 0; i < qChar.getRepetition(); i++) {
                        stringBuilder.append(qChar.getOriginalChar(i));
                    }
                }
            }

            startIndex--;
        }

        return stringBuilder.toString();
    }
}
