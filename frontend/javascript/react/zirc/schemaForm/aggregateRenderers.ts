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
import { assaysListRendererEntry } from './renderers/AssaysListRenderer';
import { genesListRendererEntry } from './renderers/GenesListRenderer';
import { lesionsListRendererEntry } from './renderers/LesionsListRenderer';
import { phenotypesListRendererEntry } from './renderers/PhenotypesListRenderer';
import { attachmentsRendererEntry } from './renderers/AttachmentsRenderer';
import { phenotypeTimingRendererEntry } from './renderers/PhenotypeTimingRenderer';

/**
 * Every field-level widget used by the aggregate forms (Mutation, Gene,
 * Lesion, Assay, Phenotype).
 *
 * Shared with {@link ZircEntityEditor}, which renders the same schemas in
 * edit mode, rather than being restated there. The two lists were near
 * duplicates and a widget registered in only one silently fell back to the
 * default renderer in the other mode — a field would look like a plain text
 * input instead of the select it declared, with nothing logged. Keeping the
 * field widgets in one place makes that impossible.
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

/**
 * View-mode registry: the field widgets plus the four child-list renderers.
 * MutationsListRenderer mounts this for each mutation card; the sibling list
 * renderers (genes / lesions / assays / phenotypes) mount it for each of
 * their child cards in view mode.
 *
 * The child-list renderers are absent from {@link fieldRenderers} because
 * ZircEntityEditor edits a single aggregate and never nests them.
 */
export const aggregateRenderers = [
    ...fieldRenderers,
    assaysListRendererEntry,
    genesListRendererEntry,
    lesionsListRendererEntry,
    phenotypesListRendererEntry,
];
