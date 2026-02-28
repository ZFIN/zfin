import React, { useState } from 'react';
import { useWizard } from '../state/WizardContext';
import { STEPS, STEP_TITLES } from '../state/stepConfig';
/* eslint-disable no-console, no-alert */
import ProgressBar from './ProgressBar';
import StepNavigation from './StepNavigation';

import Step01_GeneralInfo from '../steps/Step01_GeneralInfo';
import Step02_LineGeneralInfo from '../steps/Step02_LineGeneralInfo';
import Step03_FeatureCount from '../steps/Step03_FeatureCount';
import Step04_FeatureTypes from '../steps/Step04_FeatureTypes';
import Step05_MutationGeneralInfo from '../steps/Step05_MutationGeneralInfo';
import Step06_GeneInfo from '../steps/Step06_GeneInfo';
import Step07_LesionCount from '../steps/Step07_LesionCount';
import Step08_LesionMolecular from '../steps/Step08_LesionMolecular';
import Step09_Genotyping from '../steps/Step09_Genotyping';
import Step10_Phenotyping from '../steps/Step10_Phenotyping';
import Step11_Lethality from '../steps/Step11_Lethality';
import Step12_LinkedFeatures from '../steps/Step12_LinkedFeatures';
import Step13_LineBackground from '../steps/Step13_LineBackground';
import Step14_AdditionalInfo from '../steps/Step14_AdditionalInfo';
import StepReviewSubmit from '../steps/StepReviewSubmit';

const STEP_COMPONENTS = {
    [STEPS.GENERAL_INFO]: Step01_GeneralInfo,
    [STEPS.LINE_GENERAL_INFO]: Step02_LineGeneralInfo,
    [STEPS.FEATURE_COUNT]: Step03_FeatureCount,
    [STEPS.FEATURE_TYPES]: Step04_FeatureTypes,
    [STEPS.MUTATION_GENERAL_INFO]: Step05_MutationGeneralInfo,
    [STEPS.GENE_INFO]: Step06_GeneInfo,
    [STEPS.LESION_COUNT]: Step07_LesionCount,
    [STEPS.LESION_MOLECULAR]: Step08_LesionMolecular,
    [STEPS.GENOTYPING]: Step09_Genotyping,
    [STEPS.PHENOTYPING]: Step10_Phenotyping,
    [STEPS.LETHALITY]: Step11_Lethality,
    [STEPS.LINKED_FEATURES]: Step12_LinkedFeatures,
    [STEPS.LINE_BACKGROUND]: Step13_LineBackground,
    [STEPS.ADDITIONAL_INFO]: Step14_AdditionalInfo,
    [STEPS.REVIEW]: StepReviewSubmit,
};

const WizardShell = () => {
    const { state } = useWizard();
    const [submitting, setSubmitting] = useState(false);

    const StepComponent = STEP_COMPONENTS[state.currentStep];

    const handleSubmit = async () => {
        setSubmitting(true);
        try {
            // TODO: wire up actual submission API
            console.log('Submitting:', JSON.stringify(state, null, 2));
            alert('Submission successful!');
        } catch (err) {
            alert('Submission failed: ' + err.message);
        } finally {
            setSubmitting(false);
        }
    };

    // Context label for nested steps
    const getContextLabel = () => {
        const parts = [];
        if (state.currentStep >= STEPS.LINE_GENERAL_INFO && state.currentStep <= STEPS.ADDITIONAL_INFO) {
            parts.push(`Line ${state.currentLineIndex + 1} of ${state.lineCount}`);
        }
        if (state.currentStep >= STEPS.MUTATION_GENERAL_INFO && state.currentStep <= STEPS.LETHALITY) {
            const line = state.lines[state.currentLineIndex];
            parts.push(`Mutation ${state.currentMutationIndex + 1} of ${line.mutationCount}`);
        }
        if (state.currentStep === STEPS.GENE_INFO) {
            const mutation = state.lines[state.currentLineIndex].mutations[state.currentMutationIndex];
            parts.push(`Gene ${state.currentGeneIndex + 1} of ${mutation.geneCount}`);
        }
        if (state.currentStep === STEPS.LESION_MOLECULAR) {
            const mutation = state.lines[state.currentLineIndex].mutations[state.currentMutationIndex];
            parts.push(`Lesion ${state.currentLesionIndex + 1} of ${mutation.lesionCount}`);
        }
        return parts.length > 0 ? parts.join(' > ') : null;
    };

    const contextLabel = getContextLabel();

    return (
        <div className='container mt-4 mb-5'>
            <h2 className='mb-3'>ZIRC Line Submission</h2>
            <ProgressBar currentStep={state.currentStep} />

            {contextLabel && (
                <div className='alert alert-info py-2 mb-3'>
                    <small>{contextLabel}</small>
                </div>
            )}

            <div className='card'>
                <div className='card-header'>
                    <strong>{STEP_TITLES[state.currentStep]}</strong>
                </div>
                <div className='card-body'>
                    {StepComponent && <StepComponent />}
                </div>
            </div>

            <StepNavigation onSubmit={handleSubmit} submitting={submitting} />
        </div>
    );
};

export default WizardShell;
