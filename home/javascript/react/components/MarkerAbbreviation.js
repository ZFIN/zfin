import React from 'react';
import PropTypes from 'prop-types';

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

const MarkerAbbreviation = ({marker}) => {
    let className;
    const match = marker.zdbID.match(/^ZDB-([A-Za-z]+)-/)

    if (match) {
        const markerType = match[1];
        for (const typeClass of TYPE_CLASSES) {
            if (typeClass.types.indexOf(markerType) >= 0) {
                className = typeClass.className;
                break;
            }
        }
    }
    return <span className={className}>{marker.abbreviation}</span>;
};

MarkerAbbreviation.propTypes = {
    marker: PropTypes.shape({
        zdbID: PropTypes.string.isRequired,
        abbreviation: PropTypes.string.isRequired,
    })
};

export default MarkerAbbreviation;
