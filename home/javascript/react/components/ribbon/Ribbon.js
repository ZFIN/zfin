import React from 'react';
import {GenericRibbon} from '@geneontology/ribbon';

import style from './style.scss';

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

export default Ribbon;
