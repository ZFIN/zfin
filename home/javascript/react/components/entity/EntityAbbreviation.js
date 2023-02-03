import React from 'react';
import {entityType} from '../../utils/types';

const TYPE_CLASSES = [
    {
        className: '',
        types: ['EXP', 'TERM'],
    },
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
    let className = '';
    let type = '';

    const zdbID = entity.zdbID || '';
    const match = zdbID.match(/^ZDB-([A-Za-z_]+)-/);

    if (match) {
        type = match[1];
        for (const typeClass of Object.values(TYPE_CLASSES)) { // eslint-disable-line no-unused-vars
            if (typeClass.types.indexOf(type) >= 0) {
                className = typeClass.className;
                break;
            }
        }
    } else {
        console.warn('zdbID match failed on ' + zdbID);
        className = 'entity-abbreviation-error';
    }

    let linktext;
    if (['ETCONSTRCT', 'GTCONSTRCT', 'PTCONSTRCT', 'TGCONSTRCT', 'ATB'].includes(type)) {
        linktext = entity.name;
    } else if (['FISH'].includes(type)) {
        linktext = entity.name || '';
    } else if (['EXP'].includes(type)) {
        linktext = entity.conditions || '';
    } else if (['TERM'].includes(type)) {
        linktext = entity.termName || '';
    } else {
        linktext = entity.abbreviation || '';
    }

    return <span className={className}>{linktext}</span>;
};

EntityAbbreviation.propTypes = {
    entity: entityType,
};

export default EntityAbbreviation;
