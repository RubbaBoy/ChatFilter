/*
 * ChatFilter
 * Copyright (C) 2019 Craftathon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
