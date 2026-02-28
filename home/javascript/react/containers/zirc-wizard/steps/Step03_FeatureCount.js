import React from 'react';
import { useWizard } from '../state/WizardContext';
import NumberInput from '../components/NumberInput';

const Step03_FeatureCount = () => {
    const { state, updateField } = useWizard();
    const li = state.currentLineIndex;
    const line = state.lines[li];

    return (
        <div>
            <p>How many genetic/genomic features (alleles) are present in the line?</p>
            <p className='text-muted small'>
                Please include genetic/genomic features present in a line background
                (e.g., known background mutations and/or known background transgenes).
            </p>
            <div className='alert alert-secondary'>
                <strong>Definition:</strong> A genetic/genomic feature is an identifiable characteristic
                such as a mutation or a transgene.
            </div>
            <NumberInput
                label='Number of genetic/genomic features'
                id='featureCount'
                value={line.featureCount}
                onChange={(val) => updateField(['lines', li, 'featureCount'], val === '' ? '' : Number(val))}
                min={1}
                max={50}
            />
        </div>
    );
};

export default Step03_FeatureCount;
