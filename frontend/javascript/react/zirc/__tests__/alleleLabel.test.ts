import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import { alleleLabel } from '../schemaForm/renderers/MutationsListRenderer';
import type { MutationDTO } from '../api/types';

/**
 * How a mutation's allele is labelled where it is named as a whole
 * (ZFIN-10488): the symbol with the ZDB-ALT ID in parentheses.
 *
 * Worth pinning because the two fields are easy to mistake for each other.
 * alleleDesignation holds the ZDB-ALT ID once a curator has picked an existing
 * ZFIN feature, and alleleName is the abbreviation the server resolved for it
 * — so the ID is in the field whose name sounds like the symbol. Reading them
 * the other way round produces "ZDB-ALT-040701-2 (b700)", which looks
 * plausible enough to survive review.
 */

function mutation(fields: Partial<MutationDTO>): MutationDTO {
    return fields as MutationDTO;
}

describe('alleleLabel (ZFIN-10488)', () => {
    it('shows the symbol with the ZDB-ALT ID in parentheses', () => {
        assert.equal(
            alleleLabel(mutation({ alleleDesignation: 'ZDB-ALT-040701-2', alleleName: 'b700' })),
            'b700 (ZDB-ALT-040701-2)',
        );
    });

    it('falls back to the bare designation when nothing was resolved', () => {
        // A submission whose allele is not yet a ZFIN record keeps a free-text
        // symbol in alleleDesignation. "b700 (b700)" would be worse than "b700".
        assert.equal(
            alleleLabel(mutation({ alleleDesignation: 'b700', alleleName: null })),
            'b700',
        );
    });

    it('returns null when there is no designation at all', () => {
        // The callers render an em dash for this; returning "" would print an
        // empty cell instead and read as a rendering bug.
        assert.equal(alleleLabel(mutation({ alleleDesignation: null, alleleName: null })), null);
        assert.equal(alleleLabel(mutation({ alleleDesignation: '', alleleName: 'b700' })), null);
    });

    it('does not invent a label from a resolved name alone', () => {
        // alleleName without a designation should not surface: the pair is
        // populated together, so this state means something upstream is wrong
        // and a bare symbol would hide it.
        assert.equal(alleleLabel(mutation({ alleleDesignation: null, alleleName: 'b700' })), null);
    });
});
