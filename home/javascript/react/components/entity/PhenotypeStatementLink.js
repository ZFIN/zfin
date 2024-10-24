import React from 'react';

import {entityIDType} from '../../utils/types';

const PhenotypeStatementLink = ({entity}) => {
    return (
        <>
            <a href={`/action/phenotype/statement/${entity.id}`} dangerouslySetInnerHTML={{__html: entity.shortName}} rel='nofollow' />
            <a href={`/action/phenotype/statement-popup/${entity.id}`} className='popup-link data-popup-link' rel='nofollow'/>
        </>
    );
};

PhenotypeStatementLink.propTypes = {
    entity: entityIDType,
};

export default PhenotypeStatementLink;
