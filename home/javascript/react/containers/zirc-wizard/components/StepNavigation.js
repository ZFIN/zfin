import React, { useState } from 'react';
import PropTypes from 'prop-types';
import { useWizard } from '../state/WizardContext';
import { getNextStep, getPrevStep, STEPS } from '../state/stepConfig';
import { validateStep } from '../state/validation';

const StepNavigation = ({ onSubmit, submitting }) => {
    const { state, dispatch } = useWizard();
    const [validationErrors, setValidationErrors] = useState({});

    const nextInfo = getNextStep(state);
    const prevInfo = getPrevStep(state);

    const applyNavigation = (navInfo) => {
        if (navInfo.lineIndex !== undefined) {
            dispatch({ type: 'SET_CURRENT_LINE_INDEX', index: navInfo.lineIndex });
        }
        if (navInfo.mutationIndex !== undefined) {
            dispatch({ type: 'SET_CURRENT_MUTATION_INDEX', index: navInfo.mutationIndex });
        }
        if (navInfo.geneIndex !== undefined) {
            dispatch({ type: 'SET_CURRENT_GENE_INDEX', index: navInfo.geneIndex });
        }
        if (navInfo.lesionIndex !== undefined) {
            dispatch({ type: 'SET_CURRENT_LESION_INDEX', index: navInfo.lesionIndex });
        }
        dispatch({ type: 'SET_STEP', step: navInfo.step });
        setValidationErrors({});
    };

    const handleNext = () => {
        const { isValid, errors } = validateStep(state.currentStep, state);
        if (!isValid) {
            setValidationErrors(errors);
            return;
        }
        if (nextInfo) {
            applyNavigation(nextInfo);
        }
    };

    const handleBack = () => {
        if (prevInfo) {
            applyNavigation(prevInfo);
        }
    };

    const isLastStep = state.currentStep === STEPS.REVIEW;

    return (
        <div className='mt-4'>
            {Object.keys(validationErrors).length > 0 && (
                <div className='alert alert-danger mb-3'>
                    <ul className='mb-0'>
                        {Object.values(validationErrors).map((err, i) => (
                            <li key={i}>{err}</li>
                        ))}
                    </ul>
                </div>
            )}
            <div className='d-flex justify-content-between'>
                <button
                    type='button'
                    className='btn btn-outline-secondary'
                    onClick={handleBack}
                    disabled={!prevInfo}
                >
                    &larr; Back
                </button>
                {isLastStep ? (
                    <button
                        type='button'
                        className='btn btn-success'
                        onClick={onSubmit}
                        disabled={submitting}
                    >
                        {submitting ? 'Submitting...' : 'Submit'}
                    </button>
                ) : (
                    <button
                        type='button'
                        className='btn btn-primary'
                        onClick={handleNext}
                        disabled={!nextInfo}
                    >
                        Next &rarr;
                    </button>
                )}
            </div>
        </div>
    );
};

StepNavigation.propTypes = {
    onSubmit: PropTypes.func,
    submitting: PropTypes.bool,
};

export default StepNavigation;
