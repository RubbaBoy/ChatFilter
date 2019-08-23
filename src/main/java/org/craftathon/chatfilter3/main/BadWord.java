package org.craftathon.chatfilter3.main;

import org.craftathon.chatfilter3.qobjects.QChar;

import java.util.ArrayList;
import java.util.List;

public class BadWord implements Cloneable {

    private ChatFilter chatFilter;
    private List<QChar> qChars = new ArrayList<>();
    private int length = 0;
    private int priority;

    private int currentIndex = 0;
    private int letters = 0;
    private int numbers = 0;
    private int spaces = 0;

    private BadWord() {
    }

    public BadWord(ChatFilter chatFilter, String word, int priority) {
        this.chatFilter = chatFilter;
        this.priority = priority;

        QChar last = null;
        boolean inBracket = false;

        char[] chas = word.toCharArray();

        for (int i = 0; i < chas.length; i++) {
            Character cha = chas[i];

            if (cha == '{') {
                inBracket = true;
            } else if (cha == '}') {
                inBracket = false;
            } else {
                if (inBracket) {
                    if (last != null && last.equals(cha)) {
                        last.addRepetition(cha);
                    } else {
                        last = chatFilter.getQCharFor(cha).setOriginalChar(cha).setIndex(i);
                        qChars.add(last);
                    }

                    length++;
                    continue;
                }

                if (last == null || !last.equals(cha)) {
                    if (cha == '!') {
                        qChars.add(new QChar(true, i));
                    } else {
                        last = chatFilter.getQCharFor(cha).setOriginalChar(cha).setIndex(i);
                        qChars.add(last);
                        length++;
                    }
                } else {
                    last.addRepetition(cha);
                }
            }
        }
    }

    public int getPriority() {
        return this.priority;
    }

    public int getLength() {
        return this.length;
    }

    public int getLetters() {
        return this.letters;
    }

    public int getNumbers() {
        return this.numbers;
    }

    public int getSpaces() {
        return this.spaces;
    }

    public void resetTemporary() {
        this.currentIndex = 0;
        this.letters = 0;
        this.numbers = 0;
        this.spaces = 0;
        this.lastWasSpace = false;
    }

    public boolean nextAvailable() {
        return this.currentIndex < this.qChars.size();
    }

    private boolean lastWasSpace = false;

    public boolean allowedNext(QChar previous, QChar qChar) {
        QChar current = this.qChars.get(this.currentIndex);

        if (qChar.isSpace(chatFilter)) {
            lastWasSpace = true;
            this.spaces += qChar.getRepetition();
            return !current.isPlaceholder();
        } else {
            for (int i = 0; i < qChar.getRepetition(); i++) {
                if (Character.isDigit(qChar.getOriginalChar(i))) {
                    this.numbers++;
                } else {
                    this.letters++;
                }
            }

            this.currentIndex++;

            if (current.isPlaceholder()) {
                current = this.qChars.get(this.currentIndex);
                this.currentIndex++;
            }

        }

        QChar before = this.currentIndex - 2 >= 0 ? this.qChars.get(this.currentIndex - 2) : null;

        if (before != null && lastWasSpace && before.equalsIgnoreCase(qChar)) {
            lastWasSpace = false;
            currentIndex--;

            if (priority == 0) {
                return chatFilter.isSpace(previous);
            }

            return true;
        } else if (current.equalsIgnoreCase(qChar) && current.getRepetition() <= qChar.getRepetition()) {
            lastWasSpace = false;
            if (priority == 0) {
                return chatFilter.isSpace(previous) || this.currentIndex != 1;
            }

            return true;
        }

        return false;
    }

    public String getComparingString() {
        StringBuilder stringBuilder = new StringBuilder();

        this.qChars.forEach(qChar -> {
            if (!qChar.isPlaceholder()) {
                for (int i = 0; i < qChar.getRepetition(); i++) {
                    stringBuilder.append(qChar.getOriginalChar(i));
                }
            }
        });

        return stringBuilder.toString();
    }

    @Override
    public BadWord clone() {
        BadWord badWord = new BadWord();
        badWord.qChars = this.qChars;
        badWord.length = this.length;
        badWord.priority = this.priority;

        return badWord;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        this.qChars.forEach(qChar -> stringBuilder.append(qChar.isPlaceholder() ? "!" : qChar.toString()));

        return stringBuilder.toString();
    }

    public String toString(boolean clean) {
        if (!clean) return toString();

        StringBuilder stringBuilder = new StringBuilder();

        this.qChars.forEach(qChar -> {
            if (qChar.isPlaceholder()) {
                stringBuilder.append("!");
            } else {
                for (int i = 0; i < qChar.getRepetition(); i++) {
                    stringBuilder.append(qChar.getOriginalChar(i));
                }
            }
        });

        return stringBuilder.toString();
    }
}
