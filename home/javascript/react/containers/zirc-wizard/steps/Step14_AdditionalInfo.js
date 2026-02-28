import React from 'react';
import { useWizard } from '../state/WizardContext';
import WizardField from '../components/WizardField';
import RadioGroup from '../components/RadioGroup';
import ConditionalField from '../components/ConditionalField';

const YES_NO = [
    { value: 'yes', label: 'Yes' },
    { value: 'no', label: 'No' },
];

const Step14_AdditionalInfo = () => {
    const { state, updateField } = useWizard();
    const li = state.currentLineIndex;
    const info = state.lines[li].additionalInfo;

    const path = (field) => ['lines', li, 'additionalInfo', field];

    return (
        <div>
            <div className='form-group row'>
                <label className='col-md-4 col-form-label'>Any additional information about the line?</label>
                <div className='col-md-6'>
                    <RadioGroup
                        name='hasAdditional'
                        options={YES_NO}
                        value={info.hasAdditional === true ? 'yes' : info.hasAdditional === false ? 'no' : ''}
                        onChange={(val) => updateField(path('hasAdditional'), val === 'yes')}
                        inline
                    />
                </div>
            </div>

            <ConditionalField condition={info.hasAdditional === true}>
                <WizardField
                    label='Please specify'
                    id='additionalDetail'
                    value={info.detail}
                    onChange={(val) => updateField(path('detail'), val)}
                    tag='textarea'
                    rows={5}
                />
            </ConditionalField>
        </div>
    );
};

export default Step14_AdditionalInfo;
