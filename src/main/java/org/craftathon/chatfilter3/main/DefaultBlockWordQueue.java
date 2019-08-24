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

import org.craftathon.chatfilter3.qobjects.QChar;
import org.craftathon.chatfilter3.qobjects.QString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultBlockWordQueue implements BlockWordQueue {

    private ChatFilter chatFilter;
    private QString qString;
    private List<BlockedWord> blocked = Collections.synchronizedList(new ArrayList<>());
    private long time;

    public DefaultBlockWordQueue(ChatFilter chatFilter, QString qString) {
        this.chatFilter = chatFilter;
        this.qString = qString;
    }

    @Override
    public void addWord(BlockedWord blockedWord) {
        this.blocked.add(blockedWord);
    }

    @Override
    public void sort() {
        this.blocked.sort((blocked1, blocked2) -> blocked1.getIndex() < blocked2.getIndex() ? -1 : 1);
    }

    @Override
    public List<BlockedWord> getBlocked() {
        return blocked;
    }

    @Override
    public QString getQString() {
        return qString;
    }

    @Override
    public void removeOverlaps() {
        final int size = blocked.size();
        for (int i = 0; i < size; i++) {
            BlockedWord wonderingPrevious = null;

            BlockedWord previous = i > 0 ? blocked.get(i - 1) : null;
            BlockedWord current = blocked.get(i);
            BlockedWord next = i + 1 < blocked.size() ? blocked.get(i + 1) : null;

            if (current == null) continue;

            if (previous != null) {
                boolean previousSplicing = previous.getBadWord().getLength() + previous.getIndex() >= current.getIndex();

                if (previousSplicing) {
                    if (previous.getBadWord().getPriority() > current.getIndex()) {
                        wonderingPrevious = previous;
                    } else if (previous.getBadWord().getPriority() < current.getIndex()) {
                        blocked.set(i - 1, null);
                    } else {
                        blocked.set(i, null);
                    }
                }
            }

            if (next == null) {
                if (wonderingPrevious != null) {
                    blocked.set(i, null);
                }

                continue;
            }

            boolean splicingNext = current.getBadWord().getLength() + current.getIndex() >= next.getIndex();

            if (splicingNext) {
                if (next.getBadWord().getPriority() > current.getBadWord().getPriority()) {
                    blocked.set(i, null);
                } else {
                    blocked.set(i + 1, null);

                    if (wonderingPrevious != null) {
                        blocked.set(i - 1, null);
                    }
                }
            } else if (wonderingPrevious != null) {
                blocked.set(i, null);
            }
        }

        while (blocked.remove(null));
    }

    @Override
    public String applyBlocks() {
        var stringBuilder = new StringBuilder();
        int currentIndex = 0;
        BlockedWord nextBlocked = this.blocked.size() > 0 ? this.blocked.get(currentIndex) : null;

        if (nextBlocked == null) {
            for (int i = 0; i < this.qString.getIteratingLength(); i++) {
                QChar qChar = this.qString.qCharAt(i);

                for (int i1 = 0; i1 < qChar.getRepetition(); i1++) {
                    stringBuilder.append(qChar.getOriginalChar(i1));
                }
            }

            return stringBuilder.toString();
        }

        // The QChar index the swear starts at
        int from = nextBlocked.getIndex();
        // The QChar index the swear ends at
        int length = nextBlocked.getLength();

        // The amount of actual string characters that has been processed
        int wentThrough = 0;

        for (int i = 0; i < this.qString.getIteratingLength(); i++) {
            QChar qChar = this.qString.qCharAt(i);
            int index = qChar.getIndex();

            // If it is within the word bounds, go on
            if (index >= from && index <= from + length) {
                for (int i1 = 0; i1 < qChar.getRepetition(); i1++) {
                    if (wentThrough >= length) {
                        stringBuilder.append(qChar.getOriginalChar(i1));
                        continue;
                    }

                    wentThrough++;
                    stringBuilder.append("*");
                }

                continue;
                // If it reached the end of a word, choose another
            } else if (wentThrough >= length) {
                if (this.blocked.size() > currentIndex + 1) {
                    currentIndex++;
                    nextBlocked = this.blocked.get(currentIndex);
                    from = nextBlocked.getIndex();
                    length = nextBlocked.getLength();
                    wentThrough = 0;

                    if (index >= from) {
                        for (int i1 = 0; i1 < qChar.getRepetition(); i1++) {
                            if (wentThrough >= length) {
                                stringBuilder.append(qChar.getOriginalChar(i1));
                                continue;
                            }

                            wentThrough++;
                            stringBuilder.append("*");
                        }

                        continue;
                    }
                }
            }

            for (int i1 = 0; i1 < qChar.getRepetition(); i1++) {
                stringBuilder.append(qChar.getOriginalChar(i1));
            }
        }

        return stringBuilder.toString();
    }

    @Override
    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public long getTime() {
        return time;
    }
}
