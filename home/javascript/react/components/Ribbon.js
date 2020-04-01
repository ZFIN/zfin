import React from 'react';
import {GenericRibbon} from '@geneontology/ribbon';

import style from './style.scss';

const getSelectedTermQueryParams = (selected) => {
    let termQuery = '';
    if (selected) {
        if (selected.group.type !== 'GlobalAll') {
            termQuery += `?termId=${selected.group.id}`;
        }
        if (selected.group.type === 'Other') {
            termQuery += '&isOther=true';
        }
    }
    return termQuery;
};

const Ribbon = (props) => (
    <div className='ontology-ribbon-container'>
        <GenericRibbon
            hideFirstSubjectLabel
            colorBy={1} // annotations
            binaryColor
            maxColor={[style.primaryR, style.primaryG, style.primaryB]}
            {...props}
        />
    </div>
);

export { getSelectedTermQueryParams };
export default Ribbon;
