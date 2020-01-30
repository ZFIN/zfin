import React from 'react';
import PropTypes from 'prop-types';

const PubTrackerPanel = ({children, title}) => {
    return (
        <div className='card mb-3'>
            <h5 className='card-header'>
                {title}
            </h5>
            <div className='card-body'>
                {children}
            </div>
        </div>
    );
};

PubTrackerPanel.propTypes = {
    children: PropTypes.node,
    title: PropTypes.node,
};

export default PubTrackerPanel;
