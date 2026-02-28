import { STEPS } from './stepConfig';

export function validateStep(stepNumber, state) {
    const errors = {};
    const line = state.lines[state.currentLineIndex];
    const mutation = line?.mutations?.[state.currentMutationIndex];

    switch (stepNumber) {
    case STEPS.GENERAL_INFO:
        if (!state.lineCount || state.lineCount < 1) {
            errors.lineCount = 'Please specify at least 1 line.';
        }
        break;

    case STEPS.LINE_GENERAL_INFO:
        if (!line.name.trim()) {errors.name = 'Line name is required.';}
        if (line.acceptanceReasons.length === 0) {errors.acceptanceReasons = 'Please select at least one reason.';}
        break;

    case STEPS.FEATURE_COUNT:
        if (!line.featureCount || line.featureCount < 1) {
            errors.featureCount = 'Please specify at least 1 feature.';
        }
        break;

    case STEPS.FEATURE_TYPES:
        if (line.mutationCount + line.transgeneCount < 1) {
            errors.featureTypes = 'Please specify at least one mutation or transgene.';
        }
        break;

    case STEPS.MUTATION_GENERAL_INFO:
        if (!mutation.alleleDesignation.trim()) {errors.alleleDesignation = 'Allele designation is required.';}
        if (!mutation.mutagenesisProtocol.trim()) {errors.mutagenesisProtocol = 'Mutagenesis protocol is required.';}
        break;

    case STEPS.GENE_INFO: {
        const gene = mutation.genes[state.currentGeneIndex];
        if (!gene.mutatedGene.trim()) {errors.mutatedGene = 'Gene name is required.';}
        break;
    }

    case STEPS.LESION_MOLECULAR: {
        const lesion = mutation.lesions[state.currentLesionIndex];
        if (!lesion.type) {errors.type = 'Please select a lesion type.';}
        break;
    }

    case STEPS.LINE_BACKGROUND:
        if (!line.background.maternal.trim()) {errors.maternal = 'Maternal background is required.';}
        if (!line.background.paternal.trim()) {errors.paternal = 'Paternal background is required.';}
        break;

    default:
        break;
    }

    return {
        isValid: Object.keys(errors).length === 0,
        errors,
    };
}
