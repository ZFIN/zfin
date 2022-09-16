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
            label: 'Antibody',
            content: ({antibody}) => <EntityLink entity={antibody}/>,
            width: '150px',
        },
        {
            label: 'Gene',
            content: ({genes}) => <EntityList entities={genes}/>,
            width: '120px',
        },
        {
            label: 'Term',
            content: ({anatomyItem}) => <EntityLink entity={anatomyItem}/>,
            width: '120px',
        },
        {
            label: 'Figures',
            content: row => (
                <FigureSummary
                    statistics={row}
                    allFiguresUrl={`/action/expression/fish-expression-figure-summary?fishID=${row.antibody.zdbID}&imagesOnly=false`}
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
                <DataTableSummaryToggle detailLabel='Including children' showPopup={directAnnotation} onChange={setDirectAnnotation}/>
            )}
            <DataTable
                columns={columns}
                dataUrl={`/action/api/ontology/${termId}/antibodies?${qs.stringify(params)}`}
                onDataLoaded={() => setHasData(true)}
                rowKey={row => row.antibody.zdbID}
            />
        </>
    );
};

TermAntibodyTable.propTypes = {
    termId: PropTypes.string,
    directAnnotationOnly: PropTypes.string,
};

export default TermAntibodyTable;
