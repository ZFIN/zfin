import React from 'react';
import PropTypes from 'prop-types';

const CommaSeparatedList = ({children}) => (
    <ul className='comma-separated'>
        {React.Children.map(children, (child, idx) => (
            [<li key={idx}>{child}</li>, ' ']
        ))}
    </ul>
);

CommaSeparatedList.propTypes = {
    children: PropTypes.node,
};

export default CommaSeparatedList;
