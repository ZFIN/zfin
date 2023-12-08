package org.zfin.uniprot.adapter;

import org.biojavax.bio.seq.io.RichStreamReader;

import java.util.NoSuchElementException;

import org.biojava.bio.BioException;

public class RichStreamReaderAdapter {
    private final RichStreamReader originalStreamReader;

    public RichStreamReaderAdapter(RichStreamReader reader) {
        this.originalStreamReader = reader;
    }

    public RichSequenceAdapter nextRichSequence() throws NoSuchElementException, BioException {
        return new RichSequenceAdapter(originalStreamReader.nextRichSequence());
    }

    public boolean hasNext() {
        return originalStreamReader.hasNext();
    }

}
