package org.craftathon.chatfilter3.main;

import org.craftathon.chatfilter3.qobjects.QChar;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DefaultBadWord implements BadWord {

    private ChatFilter chatFilter;
    private List<QChar> qChars = new ArrayList<>();
    private int length = 0;
    private int priority;

    private int currentIndex = 0;
    private int letters = 0;
    private int numbers = 0;
    private int spaces = 0;

    private DefaultBadWord() {
    }

    public DefaultBadWord(ChatFilter chatFilter, String word, int priority) {
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

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public int getLength() {
        return this.length;
    }

    @Override
    public int getLetters() {
        return this.letters;
    }

    @Override
    public int getNumbers() {
        return this.numbers;
    }

    @Override
    public int getSpaces() {
        return this.spaces;
    }

    @Override
    public void resetTemporary() {
        this.currentIndex = 0;
        this.letters = 0;
        this.numbers = 0;
        this.spaces = 0;
        this.lastWasSpace = false;
    }

    @Override
    public boolean nextAvailable() {
        return this.currentIndex < this.qChars.size();
    }

    private boolean lastWasSpace = false;

    @Override
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

    @Override
    public String getComparingString() {
        return qChars.stream().filter(Predicate.not(QChar::isPlaceholder)).map(this::getRepeated).collect(Collectors.joining());
    }

    @Override
    public BadWord clone() {
        DefaultBadWord badWord = new DefaultBadWord();
        badWord.qChars = this.qChars;
        badWord.length = this.length;
        badWord.priority = this.priority;

        return badWord;
    }

    @Override
    public String toString() {
        return qChars.stream().map(qChar -> qChar.isPlaceholder() ? "!" : qChar.toString()).collect(Collectors.joining());
    }

    @Override
    public String toString(boolean clean) {
        return clean ? qChars.stream()
                .map(qChar -> qChar.isPlaceholder() ? "!" : getRepeated(qChar))
                .collect(Collectors.joining()) : toString();
    }

    private String getRepeated(QChar qChar) {
        return IntStream.range(0, qChar.getRepetition())
                .mapToObj(qChar::getOriginalChar)
                .map(String::valueOf)
                .collect(Collectors.joining());
    }
}