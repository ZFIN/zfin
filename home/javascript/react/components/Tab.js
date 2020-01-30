import React from 'react';
import PropTypes from 'prop-types';

const Tab = ({children, isActive, label}) => {
    return (
        <div role='tabpanel' className={`tab-pane ${isActive ? 'active' : ''}`} id={label}>
            {children}
        </div>
    );
};

Tab.propTypes = {
    children: PropTypes.node,
    isActive: PropTypes.bool,
    label: PropTypes.string.isRequired,
};

export default Tab;