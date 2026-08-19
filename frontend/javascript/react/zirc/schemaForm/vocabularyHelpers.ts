import { VocabularyTermDTO } from '../api/types';

/**
 * Display text for one controlled-vocabulary term.
 *
 * The amino-acid vocabulary is the only one carrying an abbreviation, and
 * the curation interface renders those as "Ala [A]". Appending the
 * abbreviation whenever it exists reproduces that for amino acids and leaves
 * the consequence vocabularies (abbreviation null) as plain labels, so one
 * rule covers every pick list.
 */
export function termLabel(term: VocabularyTermDTO): string {
    return term.abbreviation ? `${term.label} [${term.abbreviation}]` : term.label;
}

/**
 * Resolve a stored term ZDB ID back to its display text.
 *
 * Falls back to the raw id rather than rendering blank: a value that no
 * longer resolves (term retired, vocabulary still loading) should be visible
 * as something a curator can report, not vanish from the form.
 */
export function labelById(terms: VocabularyTermDTO[] | undefined, id: string): string {
    const match = terms?.find((t) => t.id === id);
    return match ? termLabel(match) : id;
}
