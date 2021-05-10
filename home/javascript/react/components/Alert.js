import React from 'react';
import PropTypes from 'prop-types';

const Alert = ({children, color, dismissable, onDismiss}) => {
    if (!children) {
        return null;
    }

    return (
        <div className={`alert alert-${color}`} role='alert'>
            {dismissable && <button type='button' className='close' onClick={onDismiss}>&times;</button>}
            {children}
        </div>
    );
};

Alert.propTypes = {
    children: PropTypes.node,
    color: PropTypes.string.isRequired,
    dismissable: PropTypes.bool,
    onDismiss: PropTypes.func,
};

export default Alert;
