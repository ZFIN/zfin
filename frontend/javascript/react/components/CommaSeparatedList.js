import React from 'react';
import PropTypes from 'prop-types';

const CommaSeparatedList = ({children, semicolon = false}) => (
    <ul className={`comma-separated ${semicolon ? 'semicolon' : ''}`}>
        {React.Children.map(children, (child, idx) => (
            [<li key={idx}>{child}</li>, ' ']
        ))}
    </ul>
);

CommaSeparatedList.propTypes = {
    children: PropTypes.node,
    semicolon: PropTypes.bool,
};

export default CommaSeparatedList;
