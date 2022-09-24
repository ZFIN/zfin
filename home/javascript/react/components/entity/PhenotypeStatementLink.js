import React from 'react';

import {entityIDType} from '../../utils/types';

const PhenotypeStatementLink = ({entity}) => {
    return (
        <>
            <a href={`/action/phenotype/statement/${entity.id}`}>
                {entity.displayName}
            </a>
            <a href={`/action/phenotype/statement/${entity.id}`} className='popup-link data-popup-link'/>
        </>
    );
};

PhenotypeStatementLink.propTypes = {
    entity: entityIDType,
};

export default PhenotypeStatementLink;
