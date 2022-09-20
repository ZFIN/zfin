import React, {useState} from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import DataTable from '../components/data-table';
import {EntityLink, EntityList} from '../components/entity';
import FigureSummary from '../components/FigureSummary';
import DataTableSummaryToggle from '../components/DataTableSummaryToggle';

const TermAntibodyTable = ({termId, directAnnotationOnly}) => {

    const [directAnnotation, setDirectAnnotation] = useState(directAnnotationOnly === 'true');
    const [count, setCount] = useState({'countDirect':0,'countIncludingChildren':0});

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
            content: ({term}) => <EntityLink entity={term}/>,
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
            {directAnnotationOnly && count.countIncludingChildren > 0 && (
                <DataTableSummaryToggle
                    detailLabel={`Including children (${count.countIncludingChildren})`}
                    showPopup={directAnnotation}
                    onChange={setDirectAnnotation}
                    overviewLabel={`Direct (${count.countDirect})`}
                />
            )}
            <DataTable
                columns={columns}
                dataUrl={`/action/api/ontology/${termId}/antibodies?${qs.stringify(params)}`}
                onDataLoadedCount={(count) => setCount(count)}
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
