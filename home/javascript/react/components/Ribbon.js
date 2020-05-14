import React from 'react';
import {GenericRibbon} from '@geneontology/ribbon';

import style from './style.scss';

const getSelectedTermQueryParams = (selected) => {
    let queryParams = {};
    if (selected) {
        if (selected.group.type !== 'GlobalAll') {
            queryParams.termId = selected.group.id;
        }
        if (selected.group.type === 'Other') {
            queryParams.isOther = true;
        }
    }
    return queryParams;
};

const Ribbon = (props) => (
    <div className='ontology-ribbon-container horizontal-scroll-container'>
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
