import React from 'react';
import { WizardProvider } from './zirc-wizard/state/WizardContext';
import WizardShell from './zirc-wizard/components/WizardShell';

const ZircSubmissionWizard = () => {
    return (
        <WizardProvider>
            <WizardShell />
        </WizardProvider>
    );
};

export default ZircSubmissionWizard;
