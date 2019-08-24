package org.craftathon.chatfilter3.main;

public class SimpleBlockedWord implements BlockedWord {

    private int index;
    private int length;
    private double numberPercentage;
    private String originalWord;
    private BadWord badWord;

    public SimpleBlockedWord(int index, int length, double numberPercentage, String originalWord, BadWord badWord) {
        this.index = index;
        this.length = length;
        this.numberPercentage = numberPercentage;
        this.originalWord = originalWord;
        this.badWord = badWord;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public double getNumberPercentage() {
        return numberPercentage;
    }

    @Override
    public String getOriginalWord() {
        return originalWord;
    }

    @Override
    public BadWord getBadWord() {
        return badWord;
    }

    @Override
    public String toString() {
        return "[index = " + index + ", length = " + length + ", word = " + badWord.toString(true) + "]";
    }
}
