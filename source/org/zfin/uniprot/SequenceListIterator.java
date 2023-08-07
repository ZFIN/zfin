package org.zfin.uniprot;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.SequenceIterator;
import org.biojavax.bio.seq.RichSequence;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class SequenceListIterator implements SequenceIterator {
    private final Iterator<RichSequence> iterator;

    public SequenceListIterator(List<RichSequence> sequences) {

        //iterator for list
        this.iterator = sequences.iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Sequence nextSequence() throws NoSuchElementException, BioException {
        return iterator.next();
    }

}
