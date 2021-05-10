import React, { useState } from 'react';
import PropTypes from 'prop-types';

const OverflowParagraph = ({children}) => {
    const [open, setOpen] = useState(false);
    return (
        <div
            className={`collapsible-attribute ${open ? '' : 'collapsed-attribute'}`}
            onClick={() => setOpen(prev => !prev)}
        >
            {children}
        </div>
    );
};

OverflowParagraph.propTypes = {
    children: PropTypes.node,
};

export default OverflowParagraph;
