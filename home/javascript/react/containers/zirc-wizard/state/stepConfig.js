export const STEPS = {
    GENERAL_INFO: 1,
    LINE_GENERAL_INFO: 2,
    FEATURE_COUNT: 3,
    FEATURE_TYPES: 4,
    MUTATION_GENERAL_INFO: 5,
    GENE_INFO: 6,
    LESION_COUNT: 7,
    LESION_MOLECULAR: 8,
    GENOTYPING: 9,
    PHENOTYPING: 10,
    LETHALITY: 11,
    LINKED_FEATURES: 12,
    LINE_BACKGROUND: 13,
    ADDITIONAL_INFO: 14,
    REVIEW: 15,
};

export const STEP_TITLES = {
    [STEPS.GENERAL_INFO]: 'General Information',
    [STEPS.LINE_GENERAL_INFO]: 'Line Information',
    [STEPS.FEATURE_COUNT]: 'Genetic/Genomic Features',
    [STEPS.FEATURE_TYPES]: 'Feature Types',
    [STEPS.MUTATION_GENERAL_INFO]: 'Mutation Information',
    [STEPS.GENE_INFO]: 'Gene Information',
    [STEPS.LESION_COUNT]: 'Lesions',
    [STEPS.LESION_MOLECULAR]: 'Molecular Nature of Lesion',
    [STEPS.GENOTYPING]: 'Molecular Genotyping',
    [STEPS.PHENOTYPING]: 'Phenotyping',
    [STEPS.LETHALITY]: 'Lethality',
    [STEPS.LINKED_FEATURES]: 'Linked Features',
    [STEPS.LINE_BACKGROUND]: 'Line Background',
    [STEPS.ADDITIONAL_INFO]: 'Additional Information',
    [STEPS.REVIEW]: 'Review & Submit',
};

// Returns { step, lineIndex, mutationIndex, geneIndex, lesionIndex } or null if at the end
export function getNextStep(state) {
    const { currentStep, currentLineIndex, currentMutationIndex, currentGeneIndex, currentLesionIndex } = state;
    const line = state.lines[currentLineIndex];
    const mutation = line?.mutations?.[currentMutationIndex];

    switch (currentStep) {
    case STEPS.GENERAL_INFO:
        return { step: STEPS.LINE_GENERAL_INFO };

    case STEPS.LINE_GENERAL_INFO:
        return { step: STEPS.FEATURE_COUNT };

    case STEPS.FEATURE_COUNT:
        return { step: STEPS.FEATURE_TYPES };

    case STEPS.FEATURE_TYPES:
        if (line.mutationCount > 0) {
            return { step: STEPS.MUTATION_GENERAL_INFO, mutationIndex: 0 };
        }
        return { step: STEPS.LINKED_FEATURES };

    case STEPS.MUTATION_GENERAL_INFO:
        if (mutation.isMolecularlyCharacterized && mutation.geneCount > 0) {
            return { step: STEPS.GENE_INFO, geneIndex: 0 };
        }
        return { step: STEPS.LESION_COUNT };

    case STEPS.GENE_INFO:
        // More genes for this mutation?
        if (currentGeneIndex < mutation.geneCount - 1) {
            return { step: STEPS.GENE_INFO, geneIndex: currentGeneIndex + 1 };
        }
        return { step: STEPS.LESION_COUNT };

    case STEPS.LESION_COUNT:
        if (mutation.lesionCount > 0) {
            return { step: STEPS.LESION_MOLECULAR, lesionIndex: 0 };
        }
        return { step: STEPS.GENOTYPING };

    case STEPS.LESION_MOLECULAR:
        // More lesions?
        if (currentLesionIndex < mutation.lesionCount - 1) {
            return { step: STEPS.LESION_MOLECULAR, lesionIndex: currentLesionIndex + 1 };
        }
        return { step: STEPS.GENOTYPING };

    case STEPS.GENOTYPING:
        return { step: STEPS.PHENOTYPING };

    case STEPS.PHENOTYPING:
        return { step: STEPS.LETHALITY };

    case STEPS.LETHALITY:
        // More mutations for this line?
        if (currentMutationIndex < line.mutationCount - 1) {
            return { step: STEPS.MUTATION_GENERAL_INFO, mutationIndex: currentMutationIndex + 1 };
        }
        return { step: STEPS.LINKED_FEATURES };

    case STEPS.LINKED_FEATURES:
        return { step: STEPS.LINE_BACKGROUND };

    case STEPS.LINE_BACKGROUND:
        return { step: STEPS.ADDITIONAL_INFO };

    case STEPS.ADDITIONAL_INFO:
        // More lines?
        if (currentLineIndex < state.lineCount - 1) {
            return { step: STEPS.LINE_GENERAL_INFO, lineIndex: currentLineIndex + 1, mutationIndex: 0 };
        }
        return { step: STEPS.REVIEW };

    case STEPS.REVIEW:
        return null;

    default:
        return null;
    }
}

export function getPrevStep(state) {
    const { currentStep, currentLineIndex, currentMutationIndex, currentGeneIndex, currentLesionIndex } = state;
    const line = state.lines[currentLineIndex];
    const mutation = line?.mutations?.[currentMutationIndex];

    switch (currentStep) {
    case STEPS.GENERAL_INFO:
        return null;

    case STEPS.LINE_GENERAL_INFO:
        if (currentLineIndex > 0) {
            return { step: STEPS.ADDITIONAL_INFO, lineIndex: currentLineIndex - 1 };
        }
        return { step: STEPS.GENERAL_INFO };

    case STEPS.FEATURE_COUNT:
        return { step: STEPS.LINE_GENERAL_INFO };

    case STEPS.FEATURE_TYPES:
        return { step: STEPS.FEATURE_COUNT };

    case STEPS.MUTATION_GENERAL_INFO:
        if (currentMutationIndex > 0) {
            // Go to lethality of previous mutation
            return { step: STEPS.LETHALITY, mutationIndex: currentMutationIndex - 1 };
        }
        return { step: STEPS.FEATURE_TYPES };

    case STEPS.GENE_INFO:
        if (currentGeneIndex > 0) {
            return { step: STEPS.GENE_INFO, geneIndex: currentGeneIndex - 1 };
        }
        return { step: STEPS.MUTATION_GENERAL_INFO };

    case STEPS.LESION_COUNT:
        if (mutation.isMolecularlyCharacterized && mutation.geneCount > 0) {
            return { step: STEPS.GENE_INFO, geneIndex: mutation.geneCount - 1 };
        }
        return { step: STEPS.MUTATION_GENERAL_INFO };

    case STEPS.LESION_MOLECULAR:
        if (currentLesionIndex > 0) {
            return { step: STEPS.LESION_MOLECULAR, lesionIndex: currentLesionIndex - 1 };
        }
        return { step: STEPS.LESION_COUNT };

    case STEPS.GENOTYPING:
        if (mutation.lesionCount > 0) {
            return { step: STEPS.LESION_MOLECULAR, lesionIndex: mutation.lesionCount - 1 };
        }
        return { step: STEPS.LESION_COUNT };

    case STEPS.PHENOTYPING:
        return { step: STEPS.GENOTYPING };

    case STEPS.LETHALITY:
        return { step: STEPS.PHENOTYPING };

    case STEPS.LINKED_FEATURES:
        if (line.mutationCount > 0) {
            return { step: STEPS.LETHALITY, mutationIndex: line.mutationCount - 1 };
        }
        return { step: STEPS.FEATURE_TYPES };

    case STEPS.LINE_BACKGROUND:
        return { step: STEPS.LINKED_FEATURES };

    case STEPS.ADDITIONAL_INFO:
        return { step: STEPS.LINE_BACKGROUND };

    case STEPS.REVIEW:
        // Go back to last line's additional info
        return { step: STEPS.ADDITIONAL_INFO, lineIndex: state.lineCount - 1 };

    default:
        return null;
    }
}
