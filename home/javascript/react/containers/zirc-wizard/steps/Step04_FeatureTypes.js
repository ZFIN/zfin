import React from 'react';
import { useWizard } from '../state/WizardContext';
import NumberInput from '../components/NumberInput';

const Step04_FeatureTypes = () => {
    const { state, dispatch } = useWizard();
    const li = state.currentLineIndex;
    const line = state.lines[li];

    const handleMutationCountChange = (count) => {
        const val = count === '' ? '' : Number(count);
        dispatch({ type: 'UPDATE_FIELD', path: ['lines', li, 'mutationCount'], value: val });
        if (typeof val === 'number') {
            dispatch({ type: 'SET_MUTATION_COUNT', lineIndex: li, count: val });
        }
    };

    const handleTransgeneCountChange = (count) => {
        const val = count === '' ? '' : Number(count);
        dispatch({ type: 'UPDATE_FIELD', path: ['lines', li, 'transgeneCount'], value: val });
        if (typeof val === 'number') {
            dispatch({ type: 'SET_TRANSGENE_COUNT', lineIndex: li, count: val });
        }
    };

    return (
        <div>
            <p>Types of genetic/genomic features present in the line:</p>
            <NumberInput
                label='Number of mutation(s)'
                id='mutationCount'
                value={line.mutationCount}
                onChange={handleMutationCountChange}
                min={0}
                max={50}
            />
            <NumberInput
                label='Number of transgene(s)'
                id='transgeneCount'
                value={line.transgeneCount}
                onChange={handleTransgeneCountChange}
                min={0}
                max={50}
            />
        </div>
    );
};

export default Step04_FeatureTypes;
