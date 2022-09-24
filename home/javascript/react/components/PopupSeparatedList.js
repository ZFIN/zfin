import React from 'react';
import PropTypes from 'prop-types';

const PopupSeparatedList = ({children, url, semicolon = false}) => (
    <ul className={`comma-separated ${semicolon ? 'semicolon' : ''}`}>
        {React.Children.map(children, (child, idx) => (
            [<li key={idx}>{child}<a href={url} className='popup-link data-popup-link'/></li>, ' ']
        ))}
    </ul>
);

PopupSeparatedList.propTypes = {
    children: PropTypes.node,
    url: PropTypes.string,
    semicolon: PropTypes.bool,
};

export default PopupSeparatedList;
