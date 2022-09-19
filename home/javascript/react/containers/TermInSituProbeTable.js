import React, {useState} from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import DataTable from '../components/data-table';
import {EntityLink, EntityList} from '../components/entity';
import FigureSummary from '../components/FigureSummary';
import DataTableSummaryToggle from '../components/DataTableSummaryToggle';

const TermAntibodyTable = ({termId, directAnnotationOnly}) => {

    const [directAnnotation, setDirectAnnotation] = useState(directAnnotationOnly === 'true');
    const [hasData, setHasData] = useState(false);

    const columns = [
        {
            label: 'Gene',
            content: ({genes}) => <EntityList entities={genes}/>,
            width: '120px',
        },
        {
            label: 'Probe',
            content: ({probe}) => <EntityLink entity={probe}/>,
            width: '120px',
        },
        {
            label: 'Term',
            content: ({term}) => <EntityLink entity={term}/>,
            width: '120px',
        },
        {
            label: 'Figures',
            content: row => (
                <FigureSummary
                    statistics={row}
                    allFiguresUrl={`/action/expression/fish-expression-figure-summary?fishID=${row.probe.zdbID}&imagesOnly=false`}
                />
            ),
            width: '100px',
        },

    ];

    const params = {};
    if (directAnnotation) {
        params.directAnnotation = true;
    }

    return (
        <>
            {directAnnotationOnly && hasData && (
                <DataTableSummaryToggle
                    detailLabel='Including children'
                    showPopup={directAnnotation}
                    onChange={setDirectAnnotation}
                    overviewLabel='Direct'
                />
            )}
            <DataTable
                columns={columns}
                dataUrl={`/action/api/ontology/${termId}/inSituProbes?${qs.stringify(params)}`}
                onDataLoaded={() => setHasData(true)}
                rowKey={row => row.probe.zdbID}
            />
        </>
    );
};

TermAntibodyTable.propTypes = {
    termId: PropTypes.string,
    directAnnotationOnly: PropTypes.string,
};

export default TermAntibodyTable;
