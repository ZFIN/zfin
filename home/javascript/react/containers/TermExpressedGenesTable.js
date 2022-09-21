import React, {useState} from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import DataTable from '../components/data-table';
import {EntityLink} from '../components/entity';
import FigureSummary from '../components/FigureSummary';
import DataTableSummaryToggle from '../components/DataTableSummaryToggle';

const TermExpressedGenesTable = ({termId, directAnnotationOnly}) => {

    const [directAnnotation, setDirectAnnotation] = useState(directAnnotationOnly === 'true');
    const [count, setCount] = useState({'countDirect':0,'countIncludingChildren':0});

    const columns = [
        {
            label: 'Gene',
            content: ({markerStat}) => <EntityLink entity={markerStat.gene}/>,
            width: '150px',
        },
        {
            label: 'Figures',
            content: row => (
                <FigureSummary
                    statistics={row.markerStat}
                    allFiguresUrl={`https://zfin.org/action/expression/results?geneField=myb&geneZdbID=${row.markerStat.gene.zdbID}&anatomyTermNames=kidney&anatomyTermIDs=${row.markerStat.anatomyTerm.zdbID}&journalType=ALL&includeSubstructures=false&onlyWildtype=true`}
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
                dataUrl={`/action/api/ontology/${termId}/expressed-genes?${qs.stringify(params)}`}
                onDataLoadedCount={(count) => setCount(count)}
                rowKey={row => row.markerStat.gene.zdbID}
            />
        </>
    );
};

TermExpressedGenesTable.propTypes = {
    termId: PropTypes.string,
    directAnnotationOnly: PropTypes.string,
};

export default TermExpressedGenesTable;
