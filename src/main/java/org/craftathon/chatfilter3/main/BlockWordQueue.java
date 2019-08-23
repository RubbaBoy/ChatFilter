package org.craftathon.chatfilter3.main;

import org.craftathon.chatfilter3.qobjects.QChar;
import org.craftathon.chatfilter3.qobjects.QString;

import java.util.ArrayList;
import java.util.List;

public class BlockWordQueue {

    private ChatFilter chatFilter;
    private QString qString;
    private List<BlockedWord> blocked = new ArrayList<>();
    private long time;

    public BlockWordQueue(ChatFilter chatFilter, QString qString) {
        this.chatFilter = chatFilter;
        this.qString = qString;
    }

    public void addWord(BlockedWord blockedWord) {
        this.blocked.add(blockedWord);
    }

    public void sort() {
        this.blocked.sort((blocked1, blocked2) -> blocked1.getIndex() < blocked2.getIndex() ? -1 : 1);
    }

    public List<BlockedWord> getBlocked() {
        return blocked;
    }

    public QString getQString() {
        return qString;
    }

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

    public String applyBlocks() {
        StringBuilder stringBuilder = new StringBuilder();
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

        int from = nextBlocked.getIndex();
        int length = nextBlocked.getLength();

        int wentThrough = 0;
        for (int i = 0; i < this.qString.getIteratingLength(); i++) {
            QChar qChar = this.qString.qCharAt(i);
            int index = qChar.getIndex();

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

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }
}
