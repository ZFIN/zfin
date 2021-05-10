import React from 'react';
import {entityType} from '../../utils/types';

const TYPE_CLASSES = [
    {
        className: 'construct',
        types: ['ETCONSTRCT', 'GTCONSTRCT', 'PTCONSTRCT', 'TGCONSTRCT'],
    },
    {
        className: 'genedom',
        types: ['GENE', 'GENEFAMILY', 'GENEP', 'LINCRNAG', 'LNCRNAG', 'MIRNAG', 'NCRNAG', 'PIRNAG', 'RRNAG', 'SCRNAG',
            'SNORNAG', 'SRPRNAG', 'TRNAG', 'BINDSITE', 'BR', 'DNAMO', 'EBS', 'EMR', 'ENHANCER', 'HMR', 'LCR', 'LIGANDBS',
            'MDNAB', 'NCBS', 'NCCR', 'NUCMO', 'PROMOTER', 'PROTBS', 'RNAMO', 'RR', 'TFBS', 'TLNRR', 'TRR'],
    },
];

const EntityAbbreviation = ({entity}) => {
    let className;
    const match = entity.zdbID.match(/^ZDB-([A-Za-z_]+)-/)

    if (match) {
        const type = match[1];
        for (const typeClass of TYPE_CLASSES) {
            if (typeClass.types.indexOf(type) >= 0) {
                className = typeClass.className;
                break;
            }
        }
    }

    let linktext;
    if (['ETCONSTRCT', 'GTCONSTRCT', 'PTCONSTRCT', 'TGCONSTRCT','ATB'].includes(match[1])) {
        linktext = entity.name;
    } else {
        linktext = entity.abbreviation;
    }

    return <span className={className}>{linktext}</span>;
};

EntityAbbreviation.propTypes = {
    entity: entityType,
};

export default EntityAbbreviation;
