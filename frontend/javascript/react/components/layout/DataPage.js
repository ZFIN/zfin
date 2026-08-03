import React from 'react';
import PropTypes from 'prop-types';
import { makeId } from '../../utils';

const DataPage = ({entityName, sections, children}) => {
    return (
        <div className='d-flex h-100'>
            <div className='data-page-nav-container'>
                <ul className='nav nav-pills flex-column'>
                    {entityName &&
                        <li className='nav-item w-100'>
                            <a href='#' className='back-to-top-link' title='Back to top'>
                                <h5 className='p-3 m-0 border-bottom text-truncate'>
                                    {entityName}
                                </h5>
                            </a>
                        </li>
                    }
                    {sections.map(section => (
                        <li className='nav-item' role='presentation' key={section}>
                            <a className='nav-link' href={`#${makeId(section)}`}>{section}</a>
                        </li>
                    ))}
                </ul>
            </div>

            <div className='data-page-content-container'>
                {children}
            </div>
        </div>
    );
};

DataPage.propTypes = {
    entityName: PropTypes.node,
    children: PropTypes.node,
    sections: PropTypes.array,
};

export default DataPage;
