import {
    createDefaultLine,
    createDefaultMutation,
    createDefaultTransgene,
    createDefaultGene,
    createDefaultLesion,
    createDefaultGenotypingAssay,
    createDefaultPhenotype,
} from './defaultValues';

// Deep clone + set a value at a path in a nested object (immutable)
function setNestedValue(obj, path, value) {
    if (path.length === 0) {return value;}
    const [head, ...tail] = path;
    const current = Array.isArray(obj) ? [...obj] : { ...obj };
    current[head] = setNestedValue(current[head], tail, value);
    return current;
}

// Resize an array to a target length, using a factory for new items
function resizeArray(arr, targetLength, factory) {
    if (targetLength <= 0) {return [];}
    if (targetLength <= arr.length) {return arr.slice(0, targetLength);}
    const newItems = Array.from({ length: targetLength - arr.length }, () => factory());
    return [...arr, ...newItems];
}

export default function wizardReducer(state, action) {
    switch (action.type) {
    case 'SET_STEP':
        return { ...state, currentStep: action.step };

    case 'SET_CURRENT_LINE_INDEX':
        return { ...state, currentLineIndex: action.index, currentMutationIndex: 0, currentGeneIndex: 0, currentLesionIndex: 0 };

    case 'SET_CURRENT_MUTATION_INDEX':
        return { ...state, currentMutationIndex: action.index, currentGeneIndex: 0, currentLesionIndex: 0 };

    case 'SET_CURRENT_GENE_INDEX':
        return { ...state, currentGeneIndex: action.index };

    case 'SET_CURRENT_LESION_INDEX':
        return { ...state, currentLesionIndex: action.index };

    case 'SET_CURRENT_ASSAY_INDEX':
        return { ...state, currentAssayIndex: action.index };

    case 'SET_CURRENT_PHENOTYPE_INDEX':
        return { ...state, currentPhenotypeIndex: action.index };

    case 'UPDATE_FIELD':
        return setNestedValue(state, action.path, action.value);

    case 'SET_LINE_COUNT': {
        const lines = resizeArray(state.lines, action.count, createDefaultLine);
        return { ...state, lineCount: action.count, lines };
    }

    case 'SET_MUTATION_COUNT': {
        const { lineIndex, count } = action;
        const line = state.lines[lineIndex];
        const mutations = resizeArray(line.mutations, count, createDefaultMutation);
        return setNestedValue(state, ['lines', lineIndex, 'mutations'], mutations);
    }

    case 'SET_TRANSGENE_COUNT': {
        const { lineIndex, count } = action;
        const line = state.lines[lineIndex];
        const transgenes = resizeArray(line.transgenes, count, createDefaultTransgene);
        return setNestedValue(state, ['lines', lineIndex, 'transgenes'], transgenes);
    }

    case 'SET_GENE_COUNT': {
        const { lineIndex, mutationIndex, count } = action;
        const mutation = state.lines[lineIndex].mutations[mutationIndex];
        const genes = resizeArray(mutation.genes, count, createDefaultGene);
        return setNestedValue(state, ['lines', lineIndex, 'mutations', mutationIndex, 'genes'], genes);
    }

    case 'SET_LESION_COUNT': {
        const { lineIndex, mutationIndex, count } = action;
        const mutation = state.lines[lineIndex].mutations[mutationIndex];
        const lesions = resizeArray(mutation.lesions, count, createDefaultLesion);
        return setNestedValue(state, ['lines', lineIndex, 'mutations', mutationIndex, 'lesions'], lesions);
    }

    case 'SET_GENOTYPING_ASSAY_COUNT': {
        const { lineIndex, mutationIndex, count } = action;
        const mutation = state.lines[lineIndex].mutations[mutationIndex];
        const assays = resizeArray(mutation.genotypingAssays, count, createDefaultGenotypingAssay);
        return setNestedValue(state, ['lines', lineIndex, 'mutations', mutationIndex, 'genotypingAssays'], assays);
    }

    case 'SET_PHENOTYPE_COUNT': {
        const { lineIndex, mutationIndex, count } = action;
        const mutation = state.lines[lineIndex].mutations[mutationIndex];
        const phenotypes = resizeArray(mutation.phenotypes, count, createDefaultPhenotype);
        return setNestedValue(state, ['lines', lineIndex, 'mutations', mutationIndex, 'phenotypes'], phenotypes);
    }

    default:
        return state;
    }
}
