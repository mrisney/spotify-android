package org.risney.cache.lib.policies;

import org.risney.cache.lib.CacheEntry;

import java.util.Comparator;

public enum EvictionPolicy {

    LRU(new LRUComparator()), LFU(new LFUComparator()), FIFO(new FIFOComparator()), SIZE(new SizeComparator());

    private final Comparator<CacheEntry> comparator;

    private EvictionPolicy(final Comparator<CacheEntry> comparator) {
        this.comparator = comparator;
    }

    public Comparator<CacheEntry> getComparator() {
        return comparator;
    }

    static class LFUComparator implements Comparator<CacheEntry> {

        @Override
        public int compare(final CacheEntry o1, final CacheEntry o2) {
            if (o1.equals(o2)) {
                return 0;
            }

            final int hitCountComparison = Integer.compare(o1.getHitCount(), o2.getHitCount());
            final int entryDateComparison = (hitCountComparison == 0)
                    ? Long.compare(o1.getEntryDate(), o2.getEntryDate()) : hitCountComparison;
            return (entryDateComparison == 0 ? Long.compare(o1.getId(), o2.getId()) : entryDateComparison);
        }
    }

    static class LRUComparator implements Comparator<CacheEntry> {

        @Override
        public int compare(final CacheEntry o1, final CacheEntry o2) {
            if (o1.equals(o2)) {
                return 0;
            }

            final int lastHitDateComparison = Long.compare(o1.getLastHitDate(), o2.getLastHitDate());
            return (lastHitDateComparison == 0 ? Long.compare(o1.getId(), o2.getId()) : lastHitDateComparison);
        }
    }

    static class FIFOComparator implements Comparator<CacheEntry> {

        @Override
        public int compare(final CacheEntry o1, final CacheEntry o2) {
            if (o1.equals(o2)) {
                return 0;
            }

            final int entryDateComparison = Long.compare(o1.getEntryDate(), o2.getEntryDate());
            return (entryDateComparison == 0 ? Long.compare(o1.getId(), o2.getId()) : entryDateComparison);
        }
    }

    static class SizeComparator implements Comparator<CacheEntry> {

        @Override
        public int compare(final CacheEntry o1, final CacheEntry o2) {
            if (o1.equals(o2)) {
                return 0;
            }

            final int sizeComparison = Long.compare(o2.getSize(), o1.getSize());
            return (sizeComparison == 0 ? Long.compare(o2.getSize(), o1.getSize()) : sizeComparison);
        }
    }

}