import { verticalLayoutRendererEntry } from './renderers/VerticalLayoutRenderer';
import { sectionRendererEntry } from './renderers/SectionRenderer';
import { rowControlRendererEntry } from './renderers/RowControlRenderer';
import { textareaRowRendererEntry } from './renderers/TextareaRowRenderer';
import { yesNoRadioRendererEntry } from './renderers/YesNoRadioRenderer';
import { checkboxRendererEntry } from './renderers/CheckboxRenderer';
import { selectWithOtherRendererEntry } from './renderers/SelectWithOtherRenderer';
import { publicationsListRendererEntry } from './renderers/PublicationsListRenderer';
import { autocompleteRendererEntry } from './renderers/AutocompleteRenderer';
import { autoSizeRendererEntry } from './renderers/AutoSizeRenderer';
import { nucleotideSequenceRendererEntry } from './renderers/NucleotideSequenceRenderer';
import { vocabularySelectRendererEntry } from './renderers/VocabularySelectRenderer';
import { vocabularyMultiSelectRendererEntry } from './renderers/VocabularyMultiSelectRenderer';
import { aminoAcidChangeRendererEntry } from './renderers/AminoAcidChangeRenderer';
import { attachmentsRendererEntry } from './renderers/AttachmentsRenderer';
import { phenotypeTimingRendererEntry } from './renderers/PhenotypeTimingRenderer';

/**
 * Every field-level widget used by the aggregate forms (Mutation, Gene,
 * Lesion, Assay, Phenotype).
 *
 * Shared by {@link ZircEntityEditor} (edit mode) and the aggregateRenderers
 * view-mode registry, rather than being restated in both. The two lists were
 * near duplicates and a widget registered in only one silently fell back to
 * the default renderer in the other mode — a field would look like a plain
 * text input instead of the select it declared, with nothing logged.
 *
 * Lives in its own module, apart from aggregateRenderers, to stay off the
 * import cycle: the child-list renderers import aggregateRenderers back, and
 * a consumer that dereferences the list at module scope would hit the
 * uninitialized binding.
 *
 * Includes every widget used across any of the five aggregate schemas;
 * unused entries are inert (their tester never matches).
 */
export const fieldRenderers = [
    verticalLayoutRendererEntry,
    sectionRendererEntry,
    rowControlRendererEntry,
    textareaRowRendererEntry,
    yesNoRadioRendererEntry,
    checkboxRendererEntry,
    selectWithOtherRendererEntry,
    publicationsListRendererEntry,
    autocompleteRendererEntry,
    autoSizeRendererEntry,
    nucleotideSequenceRendererEntry,
    vocabularySelectRendererEntry,
    vocabularyMultiSelectRendererEntry,
    aminoAcidChangeRendererEntry,
    attachmentsRendererEntry,
    phenotypeTimingRendererEntry,
];
