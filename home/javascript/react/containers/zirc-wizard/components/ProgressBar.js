import React from 'react';
import PropTypes from 'prop-types';
import { STEP_TITLES } from '../state/stepConfig';

const SECTION_LABELS = [
    'General',
    'Line Info',
    'Features',
    'Mutations',
    'Genotyping',
    'Phenotyping',
    'Background',
    'Review',
];

const ProgressBar = ({ currentStep, totalSections = SECTION_LABELS.length }) => {
    const stepTitle = STEP_TITLES[currentStep] || '';

    // Map step number to a rough section index for the progress indicator
    let sectionIndex = 0;
    if (currentStep <= 1) {sectionIndex = 0;}
    else if (currentStep <= 2) {sectionIndex = 1;}
    else if (currentStep <= 4) {sectionIndex = 2;}
    else if (currentStep <= 8) {sectionIndex = 3;}
    else if (currentStep <= 9) {sectionIndex = 4;}
    else if (currentStep <= 11) {sectionIndex = 5;}
    else if (currentStep <= 14) {sectionIndex = 6;}
    else {sectionIndex = 7;}

    const percent = Math.round(((sectionIndex + 1) / totalSections) * 100);

    return (
        <div className='mb-4'>
            <div className='d-flex justify-content-between mb-1'>
                <span className='font-weight-bold'>{stepTitle}</span>
                <span className='text-muted small'>{percent}%</span>
            </div>
            <div className='progress' style={{ height: '8px' }}>
                <div
                    className='progress-bar bg-primary'
                    role='progressbar'
                    style={{ width: `${percent}%` }}
                    aria-valuenow={percent}
                    aria-valuemin='0'
                    aria-valuemax='100'
                />
            </div>
            <div className='d-flex justify-content-between mt-1'>
                {SECTION_LABELS.map((label, i) => (
                    <span
                        key={label}
                        className={`small ${i <= sectionIndex ? 'text-primary font-weight-bold' : 'text-muted'}`}
                    >
                        {label}
                    </span>
                ))}
            </div>
        </div>
    );
};

ProgressBar.propTypes = {
    currentStep: PropTypes.number.isRequired,
    totalSections: PropTypes.number,
};

export default ProgressBar;
