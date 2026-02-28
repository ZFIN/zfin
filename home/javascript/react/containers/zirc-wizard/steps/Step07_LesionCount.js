import React from 'react';
import { useWizard } from '../state/WizardContext';
import RadioGroup from '../components/RadioGroup';
import NumberInput from '../components/NumberInput';
import ConditionalField from '../components/ConditionalField';

const YES_NO = [
    { value: 'yes', label: 'Yes' },
    { value: 'no', label: 'No' },
];

const Step07_LesionCount = () => {
    const { state, dispatch, updateField } = useWizard();
    const li = state.currentLineIndex;
    const mi = state.currentMutationIndex;
    const mutation = state.lines[li].mutations[mi];

    const path = (field) => ['lines', li, 'mutations', mi, field];

    const handleMultipleLesionsChange = (val) => {
        const isMultiple = val === 'yes';
        updateField(path('hasMultipleLesions'), isMultiple);
        if (!isMultiple) {
            updateField(path('lesionCount'), 1);
            dispatch({ type: 'SET_LESION_COUNT', lineIndex: li, mutationIndex: mi, count: 1 });
        }
    };

    const handleLesionCountChange = (count) => {
        const val = count === '' ? '' : Number(count);
        updateField(path('lesionCount'), val);
        if (typeof val === 'number' && val > 0) {
            dispatch({ type: 'SET_LESION_COUNT', lineIndex: li, mutationIndex: mi, count: val });
        }
    };

    return (
        <div>
            <p className='text-muted small'>Some mutations may contain multiple lesions.</p>

            <div className='form-group row'>
                <label className='col-md-4 col-form-label'>Does the mutation contain multiple lesions?</label>
                <div className='col-md-6'>
                    <RadioGroup
                        name='hasMultipleLesions'
                        options={YES_NO}
                        value={mutation.hasMultipleLesions === true ? 'yes' : mutation.hasMultipleLesions === false ? 'no' : ''}
                        onChange={handleMultipleLesionsChange}
                        inline
                    />
                </div>
            </div>

            <ConditionalField condition={mutation.hasMultipleLesions === true}>
                <NumberInput
                    label='How many lesions does the mutation contain?'
                    id='lesionCount'
                    value={mutation.lesionCount}
                    onChange={handleLesionCountChange}
                    min={1}
                    max={20}
                />
            </ConditionalField>
        </div>
    );
};

export default Step07_LesionCount;
