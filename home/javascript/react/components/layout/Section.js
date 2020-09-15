import React from 'react';
import PropTypes from 'prop-types';
import {makeId} from '../../utils';

const Section = ({children, className, title}) => {
    return (
        <section className={`section ${className}`} id={makeId(title)}>
            {title && <div className='heading'>{title}</div>}
            {children}
        </section>
    );
};

Section.propTypes = {
    children: PropTypes.node.isRequired,
    className: PropTypes.string,
    title: PropTypes.string,
};

export default Section;
