package org.zfin.uniprot;

import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.zfin.uniprot.adapter.RichSequenceAdapter;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class SequenceListIterator implements SequenceIterator {
    private final Iterator<RichSequenceAdapter> iterator;

    public SequenceListIterator(List<RichSequenceAdapter> sequences) {

        //iterator for list
        this.iterator = sequences.iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Sequence nextSequence() throws NoSuchElementException {
        return iterator.next().unwrap();
    }

}
