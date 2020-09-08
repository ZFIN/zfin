import React from 'react';
import PropTypes from 'prop-types';
import {makeId} from '../utils';

const PageNav = ({sections}) => {
    const listItem = sections.map((section, idx) => <li key={idx} className='nav-item' role='presentation'><a
        className='nav-link'
        href={'#' + makeId(section.name)}
    >{section.name}</a></li>)
    return (

        <div className='data-page-nav-container'>
            <ul className='nav nav-pills flex-column'>
                {listItem}
            </ul>


        </div>

    );
};


PageNav.propTypes = {
    sections: PropTypes.arrayOf(PropTypes.shape({
        name: PropTypes.string.isRequired,
    })),
};
export default PageNav;
