import React, { useEffect, useRef } from 'react';
import {GenericRibbon} from '@geneontology/ribbon';

import style from './style.scss';

const Ribbon = (props) => {
    const ribbonRef = useRef(null);
    useEffect(() => {
        if (!ribbonRef.current) {
            return;
        }
        ribbonRef.current.querySelectorAll('.ontology-ribbon__item').forEach(item => {
            // warning! changing the 'No annotations' text here may break a style selector in datapage.scss
            const title = item.getAttribute('title')
                .replace(/class(e?)/, 'term')
                .replace(/^0 term, 0 annotation$/, 'No annotations');
            item.setAttribute('title', title);
        })
    }, [props.subjects])
    return (
        <div className='ontology-ribbon-container horizontal-scroll-container' ref={ribbonRef}>
            <GenericRibbon
                hideFirstSubjectLabel
                colorBy={1} // annotations
                binaryColor
                maxColor={[style.primaryR, style.primaryG, style.primaryB]}
                {...props}
            />
            <small className='text-muted'>
                Click a filled square to see annotations
            </small>
        </div>
    );
}

export default Ribbon;
