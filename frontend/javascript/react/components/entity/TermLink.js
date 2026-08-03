import React from 'react';
import {termType} from '../../utils/types';

const TermLink = ({entity}) => {
    return (
        <a href={`/${entity.zdbID}`}>
            {entity.termName}
        </a>
    );
};

TermLink.propTypes = {
    entity: termType,
};

export default TermLink;
