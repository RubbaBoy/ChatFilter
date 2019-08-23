package org.craftathon.chatfilter3.main;

public class BlockedWord {

    private int index;
    private int length;
    private double numberPercentage;
    private String originalWord;
    private BadWord badWord;

    public BlockedWord(int index, int length, double numberPercentage, String originalWord, BadWord badWord) {
        this.index = index;
        this.length = length;
        this.numberPercentage = numberPercentage;
        this.originalWord = originalWord;
        this.badWord = badWord;
    }

    public int getIndex() {
        return index;
    }

    public int getLength() {
        return length;
    }

    public double getNumberPercentage() {
        return numberPercentage;
    }

    public String getOriginalWord() {
        return originalWord;
    }

    public BadWord getBadWord() {
        return badWord;
    }

    @Override
    public String toString() {
        return "[index = " + index + ", length = " + length + ", word = " + badWord.toString(true) + "]";
    }
}
