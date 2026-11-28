package org.zfin.mapping;

import java.util.Collections;
import java.util.List;

/**
 * The single flanking-sequence section of a Complex feature (ZFIN-10641): the reference upstream
 * of the lowest variant, each variant's change, the reference between consecutive variants, and
 * the reference downstream of the highest variant -- in genomic order, so the variants read in
 * the same order as the feature page's Variants section without labels.
 *
 * @param assembly the assembly the sequence was read from
 * @param segments reference and variation pieces, in genomic order
 */
public record ComplexFlankingSequence(String assembly, List<Segment> segments) {

    public ComplexFlankingSequence {
        segments = Collections.unmodifiableList(segments);
    }

    /**
     * A stretch of reference sequence, or one variant's change ("C/T", "ATGCGCC/-", "-/GG")
     * when {@code variation} is true.
     */
    public record Segment(String sequence, boolean variation) {

        // JSP EL reads bean-style getters, not record accessors
        public String getSequence() {
            return sequence;
        }

        public boolean isVariation() {
            return variation;
        }
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public String getAssembly() {
        return assembly;
    }
}
