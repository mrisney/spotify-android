package org.risney.cache.lib.policies;

import org.risney.cache.lib.CacheEntry;

import java.util.Comparator;

/**
 * EvictionPolicy is the based on Comparator of CacheEntry properties
 * to create a new EvictionPolicy, add to the ENUM EvictionPolicy, and create a mew static class the implements
 * a java.util.Comparator, comparing a properties of the CacheEntry.
 *
 * @author marc.risney@gmail.com
 * @version 1.0
 * @since 2016-06-20
 */
public enum EvictionPolicy {

    LRU(new LRUComparator()), LFU(new LFUComparator()), FIFO(new FIFOComparator()), SIZE(new SizeComparator());

    private final Comparator<CacheEntry> comparator;

    private EvictionPolicy(final Comparator<CacheEntry> comparator) {
        this.comparator = comparator;
    }

    public Comparator<CacheEntry> getComparator() {
        return comparator;
    }

    /**
     * Classic Least Recently Used - LRU Comparator, using the getLastHitDate of CacheEntry
     * getLastHitDate is updated on ImageCache.putIfAbsent(K,V) method, and also on the ImageCache.get(K) method
     * to setLastHitDate of CacheEntry - ImageCache.get(K) can be used.
     */

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

    /**
     * Least Frequently Used - LFU Comparator, using the getHitCount compared to the getEntryDate() of CacheEntry
     * getLastHitDate is also on ImageCache.putIfAbsent(K,V) method, and also on the ImageCache.get(K) method
     * to setHitCount of CacheEntry - ImageCache.get(K) can be used.
     */

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

    /**
     * First In First Out - FIFO, using the getEntryDate property of CacheEntry
     * setting the HitCount has no affect on the Comparator, getEntryDate() of CacheEntry
     */

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

    /**
     * Size of CacheEntry in Bytes
     * Size of the Key AND Value of the Entry's ByteBuffers value, is set when put into Cache.
     * The bigger the combination of Key and Value, the higher it rises to the top of candidacy for eviction.
     */

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


    /**
     * ExampleComparator - Roll your own, when your done, add it to the EvictionPolicy, with some ENUM Identifier
     * Generally a property of the CacheEntry is used to compare one Entry from another
     * For example, you could do a comparison of Dominate colors, and do some type of RGB dominate color
     * The Android Support Library r21 and above includes the Palette class, which lets you extract prominent colors from an image. To extract these colors,
     * pass a Bitmap object to the Palette.generate() static method in the background thread where you load your images
     * <p/>
     * Do a dominate color review and create and set the RGB property of CacheEntry, then olny images with prominent specific colors would be cached
     * just ideas, let your imagination run wild
     */

    static class ExampleComparator implements Comparator<CacheEntry> {
        @Override
        public int compare(final CacheEntry o1, final CacheEntry o2) {
            return 0;
        }
    }
}