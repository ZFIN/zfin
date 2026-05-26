import { verticalLayoutRendererEntry } from './renderers/VerticalLayoutRenderer';
import { sectionRendererEntry } from './renderers/SectionRenderer';
import { rowControlRendererEntry } from './renderers/RowControlRenderer';
import { textareaRowRendererEntry } from './renderers/TextareaRowRenderer';
import { yesNoRadioRendererEntry } from './renderers/YesNoRadioRenderer';
import { selectWithOtherRendererEntry } from './renderers/SelectWithOtherRenderer';
import { publicationsListRendererEntry } from './renderers/PublicationsListRenderer';
import { autocompleteRendererEntry } from './renderers/AutocompleteRenderer';
import { assaysListRendererEntry } from './renderers/AssaysListRenderer';
import { genesListRendererEntry } from './renderers/GenesListRenderer';
import { lesionsListRendererEntry } from './renderers/LesionsListRenderer';
import { phenotypesListRendererEntry } from './renderers/PhenotypesListRenderer';
import { attachmentsRendererEntry } from './renderers/AttachmentsRenderer';
import { phenotypeTimingRendererEntry } from './renderers/PhenotypeTimingRenderer';

/**
 * Renderer registry for nested per-aggregate forms (Mutation, Gene, Lesion,
 * Assay, Phenotype). MutationsListRenderer mounts this for each mutation
 * card; the four sibling list renderers (genes / lesions / assays /
 * phenotypes) mount it for each of their child cards in view mode.
 *
 * Includes every widget used across any of the five aggregate schemas;
 * unused entries are inert (their tester never matches).
 */
export const aggregateRenderers = [
    verticalLayoutRendererEntry,
    sectionRendererEntry,
    rowControlRendererEntry,
    textareaRowRendererEntry,
    yesNoRadioRendererEntry,
    selectWithOtherRendererEntry,
    publicationsListRendererEntry,
    autocompleteRendererEntry,
    assaysListRendererEntry,
    genesListRendererEntry,
    lesionsListRendererEntry,
    phenotypesListRendererEntry,
    attachmentsRendererEntry,
    phenotypeTimingRendererEntry,
];
