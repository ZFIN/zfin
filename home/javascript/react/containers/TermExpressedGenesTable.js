import React from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import DataTable from '../components/data-table';
import {EntityLink} from '../components/entity';
import FigureSummary from '../components/FigureSummary';

const TermExpressedGenesTable = ({termId}) => {


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

    return (
        <>
            <DataTable
                columns={columns}
                dataUrl={`/action/api/ontology/${termId}/expressed-genes?${qs.stringify(params)}`}
                rowKey={row => row.markerStat.gene.zdbID}
            />
        </>
    );
};

TermExpressedGenesTable.propTypes = {
    termId: PropTypes.string,
};

export default TermExpressedGenesTable;
