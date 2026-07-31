import React from 'react';
import PropTypes from 'prop-types';

const DataTableSummaryToggle = ({showPopup, onChange, directLabel = ('Direct'), childrenLabel = ('Including children'), directCount = (0), childrenCount = (0)}) => {
    return (
        <div className='mb-2'>
            <div className='btn-group btn-group-sm' role='group'>
                <button
                    type='button'
                    className={'btn btn-outline-secondary' + (showPopup ? ' active' : '')}
                    onClick={() => onChange(true)}
                >
                    {directLabel} ({directCount.toLocaleString()})
                </button>
                <button
                    type='button'
                    className={'btn btn-outline-secondary' + (!showPopup ? ' active' : '')}
                    onClick={() => directCount !== childrenCount ? onChange(false) : undefined}
                >
                    {childrenLabel} ({childrenCount.toLocaleString()})
                </button>
            </div>
        </div>
    );
}

DataTableSummaryToggle.propTypes = {
    childrenLabel: PropTypes.node,
    directLabel: PropTypes.node,
    onChange: PropTypes.func,
    showPopup: PropTypes.bool,
    directCount: PropTypes.number,
    childrenCount: PropTypes.number,
};

export default DataTableSummaryToggle;
