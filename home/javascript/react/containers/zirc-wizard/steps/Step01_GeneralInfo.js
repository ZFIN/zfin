import React from 'react';
import { useWizard } from '../state/WizardContext';
import NumberInput from '../components/NumberInput';

const Step01_GeneralInfo = () => {
    const { state, dispatch } = useWizard();

    const handleLineCountChange = (count) => {
        dispatch({ type: 'SET_LINE_COUNT', count: count === '' ? '' : Number(count) });
    };

    return (
        <div>
            <p>How many lines will be submitted?</p>
            <NumberInput
                label='Number of lines'
                id='lineCount'
                value={state.lineCount}
                onChange={handleLineCountChange}
                min={1}
                max={50}
            />
        </div>
    );
};

export default Step01_GeneralInfo;
