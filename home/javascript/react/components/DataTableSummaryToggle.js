import React from 'react';
import PropTypes from 'prop-types';

const DataTableSummaryToggle = ({detailLabel, value, onChange}) => {
    return (
        <div className='mb-2'>
            <div className='btn-group btn-group-sm' role='group'>
                <button
                    type='button'
                    className={'btn btn-outline-secondary' + (value ? ' active' : '')}
                    onClick={() => onChange(true)}
                >
                    Overview
                </button>
                <button
                    type='button'
                    className={'btn btn-outline-secondary' + (!value ? ' active' : '')}
                    onClick={() => onChange(false)}
                >
                    {detailLabel}
                </button>
            </div>
        </div>
    );
}

DataTableSummaryToggle.propTypes = {
    detailLabel: PropTypes.node,
    onChange: PropTypes.func,
    value: PropTypes.bool,
};

export default DataTableSummaryToggle;
