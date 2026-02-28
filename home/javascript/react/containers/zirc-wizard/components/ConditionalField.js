import React from 'react';
import PropTypes from 'prop-types';

const ConditionalField = ({ condition, children }) => {
    if (!condition) {return null;}
    return <>{children}</>;
};

ConditionalField.propTypes = {
    condition: PropTypes.bool.isRequired,
    children: PropTypes.node.isRequired,
};

export default ConditionalField;
