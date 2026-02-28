import React from 'react';
import { useWizard } from '../state/WizardContext';
import WizardField from '../components/WizardField';
import CheckboxGroup from '../components/CheckboxGroup';

const ACCEPTANCE_REASONS = [
    { value: 'unique_phenotype', label: 'Unique phenotype' },
    { value: 'disease_model', label: 'Disease model' },
    { value: 'widely_used', label: 'Widely used by the community' },
    { value: 'published', label: 'Published line' },
    { value: 'reporter_line', label: 'Reporter/driver line' },
    { value: 'crispr_mutant', label: 'CRISPR mutant' },
    { value: 'enu_mutant', label: 'ENU mutant' },
    { value: 'transgenic', label: 'Transgenic line' },
    { value: 'other', label: 'Other' },
];

const Step02_LineGeneralInfo = () => {
    const { state, updateField } = useWizard();
    const li = state.currentLineIndex;
    const line = state.lines[li];

    const path = (field) => ['lines', li, field];

    return (
        <div>
            <WizardField
                label='Line name'
                id='lineName'
                value={line.name}
                onChange={(val) => updateField(path('name'), val)}
            />
            <WizardField
                label='Line name abbreviation'
                id='lineAbbreviation'
                value={line.abbreviation}
                onChange={(val) => updateField(path('abbreviation'), val)}
            />
            <WizardField
                label='Previous line names'
                id='previousNames'
                value={line.previousNames}
                onChange={(val) => updateField(path('previousNames'), val)}
                tag='textarea'
                rows={2}
            />

            <div className='form-group row'>
                <label className='col-md-4 col-form-label'>Reasons why ZIRC should accept the line</label>
                <div className='col-md-6'>
                    <CheckboxGroup
                        name='acceptanceReasons'
                        options={ACCEPTANCE_REASONS}
                        values={line.acceptanceReasons}
                        onChange={(val) => updateField(path('acceptanceReasons'), val)}
                    />
                </div>
            </div>
        </div>
    );
};

export default Step02_LineGeneralInfo;
