package org.zfin.sequence.gff;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Spliterator.ORDERED;

@Log4j2
public class BatchIterator<T> implements Iterator<List<T>> {
    private final int batchSize;
    private List<T> currentBatch;
    private final Iterator<T> iterator;

    private BatchIterator(Iterator<T> sourceIterator, int batchSize) {
        this.batchSize = batchSize;
        this.iterator = sourceIterator;
    }

    @Override
    public List<T> next() {
        prepareNextBatch();
        return currentBatch;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    private void prepareNextBatch() {
        currentBatch = new ArrayList<>(batchSize);
        while (iterator.hasNext() && currentBatch.size() < batchSize) {
            currentBatch.add(iterator.next());
        }
    }

    public static <T> Stream<List<T>> batchStreamOf(Stream<T> stream, int batchSize) {
        return stream(new BatchIterator<>(stream.iterator(), batchSize));
    }
    private static <T> Stream<T> stream(Iterator<T> iterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, ORDERED), false);
    }
}


