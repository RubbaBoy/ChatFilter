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

package org.craftathon.chatfilter3.qobjects;

import org.craftathon.chatfilter3.main.ChatFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class QChar implements Cloneable {

    private List<Character> values = new ArrayList<>();
    private List<Character> repeated = new ArrayList<>();
    private int repetition = 1;
    private Character originalChar = null;
    private boolean isPlaceholder = false;
    private int index = -1;

    public QChar(boolean isPlaceholder, int index) {
        this.isPlaceholder = isPlaceholder;
        this.index = index;

        this.repetition = isPlaceholder ? 0 : 1;
    }

    public QChar(int index, Character... multiple) {
        this.index = index;
        this.values = Arrays.stream(multiple).collect(Collectors.toList());
    }

    public QChar(String formattedQChar) {
        try {
            if (formattedQChar.length() < 3 && formattedQChar.startsWith("(") && formattedQChar.endsWith(")"))
                throw new InvalidQCharFormatException("Invalid length or doesn't have beginning/end parentheses");
            formattedQChar = formattedQChar.substring(1, formattedQChar.length() - 1);
            this.values.addAll(splitCharArray(formattedQChar.toLowerCase().toCharArray()));
        } catch (InvalidQCharFormatException e) {
            e.printStackTrace();
        }
    }

    private List<Character> splitCharArray(char[] arr) throws QChar.InvalidQCharFormatException {
        List<Character> ret = new ArrayList<>();
        boolean getChar = true;
        int index = 0;
        for (char cha : arr) {
            index++;
            if (getChar) {
                ret.add(cha);
            } else {
                if (cha != '|') {
                    throw new InvalidQCharFormatException("No pipe separator found at index " + index);
                }
            }

            getChar ^= true;
        }

        return ret;
    }

    public QChar setOriginalChar(Character character) {
        this.originalChar = Character.toLowerCase(character);
        this.repeated.add(character);
        return this;
    }

    public Character getOriginalChar(int index) {
        return this.repeated.get(index);
    }

    public boolean isPlaceholder() {
        return isPlaceholder;
    }

    public QChar setIndex(int index) {
        this.index = index;
        return this;
    }

    public int getIndex() {
        return this.index;
    }

    public void addRepetition(Character character) {
        this.repeated.add(character);
        this.repetition++;
    }

    public int getRepetition() {
        return this.repetition;
    }

    public boolean equals(QChar qChar) {
        return qChar.values.size() < this.values.size() ? this.values.containsAll(qChar.values) : qChar.values.containsAll(this.values);
    }

    public boolean equals(Character character) {
        return this.values.contains(character);
    }

    public boolean equalsExact(QChar qChar) {
        return this.values.equals(qChar.values);
    }

    public boolean equalsExact(Character character) {
        return this.values.size() == 1 && this.values.get(0) == character;
    }

    public boolean equalsIgnoreCase(QChar qChar) {
        var loopingFrom = qChar.values.size() > this.values.size() ? toLowercaseList(qChar.values) : toLowercaseList(this.values);
        var gettingFrom = qChar.values.size() > this.values.size() ? toLowercaseList(this.values) : toLowercaseList(qChar.values);

        return loopingFrom.stream().anyMatch(gettingFrom::contains);
    }

    public boolean equalsIgnoreCase(Character character) {
        return toLowercaseList(this.values).contains(Character.toLowerCase(character));
    }

    private List<Character> toLowercaseList(List<Character> characterList) {
        return characterList.stream().map(Character::toLowerCase).collect(Collectors.toList());
    }

    @Override
    public QChar clone() {
        QChar ret = new QChar(-1, ' ');
        ret.values = this.values;
        ret.repetition = this.repetition;
        ret.originalChar = this.originalChar;
        ret.index = this.index;

        return ret;
    }

    @Override
    public String toString() {
        if (values.size() == 0) return "(" + ((index == -1) ? "" : index) + ")";

        StringBuilder b = new StringBuilder();
        b.append("(");
        b.append(repetition != 1 ? "x" + repetition + "|" : "");
        b.append(index == -1 ? "" : index + "|");

        for (int i = 0; i < values.size(); i++) {
            b.append(values.get(i));
            if (i == values.size() - 1)
                return b.append(')').toString();
            b.append("|");
        }
        return b.toString();
    }

    public List<Character> getCharValues() {
        return this.values;
    }

    public boolean isSpace(ChatFilter chatFilter) {
        return chatFilter.isSpace(this);
    }

    public static class InvalidQCharFormatException extends Exception {
        InvalidQCharFormatException(String message) {
            super(message);
        }
    }

}
