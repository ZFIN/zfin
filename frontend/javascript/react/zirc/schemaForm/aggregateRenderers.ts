import { fieldRenderers } from './fieldRenderers';
import { assaysListRendererEntry } from './renderers/AssaysListRenderer';
import { genesListRendererEntry } from './renderers/GenesListRenderer';
import { lesionsListRendererEntry } from './renderers/LesionsListRenderer';
import { phenotypesListRendererEntry } from './renderers/PhenotypesListRenderer';

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
