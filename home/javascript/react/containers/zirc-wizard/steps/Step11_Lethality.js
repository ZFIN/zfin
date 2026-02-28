import React from 'react';
import { useWizard } from '../state/WizardContext';
import WizardField from '../components/WizardField';
import RadioGroup from '../components/RadioGroup';
import ConditionalField from '../components/ConditionalField';

const YES_NO = [
    { value: 'yes', label: 'Yes' },
    { value: 'no', label: 'No' },
];

const DEATH_STAGE_TYPE = [
    { value: 'specific', label: 'A specific developmental time point' },
    { value: 'window', label: 'A temporal window' },
];

const Step11_Lethality = () => {
    const { state, updateField } = useWizard();
    const li = state.currentLineIndex;
    const mi = state.currentMutationIndex;
    const mutation = state.lines[li].mutations[mi];

    const path = (field) => ['lines', li, 'mutations', mi, field];

    return (
        <div>
            <div className='form-group row'>
                <label className='col-md-4 col-form-label'>Is the mutation homozygous lethal?</label>
                <div className='col-md-6'>
                    <RadioGroup
                        name='isHomozygousLethal'
                        options={YES_NO}
                        value={mutation.isHomozygousLethal === true ? 'yes' : mutation.isHomozygousLethal === false ? 'no' : ''}
                        onChange={(val) => updateField(path('isHomozygousLethal'), val === 'yes')}
                        inline
                    />
                </div>
            </div>

            <ConditionalField condition={mutation.isHomozygousLethal === true}>
                <p>Specify developmental stage at which homozygous individuals die:</p>
                <div className='mb-3'>
                    <RadioGroup
                        name='stageOfDeathType'
                        options={DEATH_STAGE_TYPE}
                        value={mutation.stageOfDeathType}
                        onChange={(val) => updateField(path('stageOfDeathType'), val)}
                    />
                </div>

                <ConditionalField condition={mutation.stageOfDeathType === 'specific'}>
                    <WizardField
                        label='Stage'
                        id='stageOfDeathSpecific'
                        value={mutation.stageOfDeathSpecific}
                        onChange={(val) => updateField(path('stageOfDeathSpecific'), val)}
                    />
                </ConditionalField>

                <ConditionalField condition={mutation.stageOfDeathType === 'window'}>
                    <WizardField
                        label='From'
                        id='stageOfDeathFrom'
                        value={mutation.stageOfDeathFrom}
                        onChange={(val) => updateField(path('stageOfDeathFrom'), val)}
                    />
                    <WizardField
                        label='To'
                        id='stageOfDeathTo'
                        value={mutation.stageOfDeathTo}
                        onChange={(val) => updateField(path('stageOfDeathTo'), val)}
                    />
                </ConditionalField>
            </ConditionalField>

            <WizardField
                label='Additional information'
                id='lethalityAdditionalInfo'
                value={mutation.lethalityAdditionalInfo}
                onChange={(val) => updateField(path('lethalityAdditionalInfo'), val)}
                tag='textarea'
                rows={3}
            />
        </div>
    );
};

export default Step11_Lethality;
