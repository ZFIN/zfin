import React, {useState} from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import DataTable from '../components/data-table';
import {EntityList} from '../components/entity';
import DataTableSummaryToggle from '../components/DataTableSummaryToggle';
import CommaSeparatedList from '../components/CommaSeparatedList';
import PhenotypeStatementLink from '../components/entity/PhenotypeStatementLink';
import FigureSummary from '../components/FigureSummary';

const TermPhenotypeTable = ({termId, directAnnotationOnly}) => {

    const [directAnnotation, setDirectAnnotation] = useState(directAnnotationOnly === 'true');
    const [count, setCount] = useState({'countDirect': 0, 'countIncludingChildren': 0});

    const columns = [
        {
            label: 'Affected Genomic Region',
            content: ({fish}) => <EntityList entities={fish.affectedGenes}/>,
            width: '100px',
        },
        {
            label: 'Fish',
            content: ({fish}) => <a
                href={'/' + fish.zdbID}
                dangerouslySetInnerHTML={{__html: fish.name}}
            />,
            filterName: 'fishName',
            width: '120px',
        },
        {
            label: 'Phenotype',
            content: ({phenotypeObserved}) => <CommaSeparatedList>
                {phenotypeObserved.map(entity => {
                    return <PhenotypeStatementLink key={entity.id} entity={entity}/>
                })}
            </CommaSeparatedList>,
            filterName: 'phenotype',
            width: '220px',
        },
        {
            label: 'Figures',
            content: row => (
                <FigureSummary
                    statistics={row}
                    allFiguresUrl={`/action/ontology/${row.anatomyItem.zdbID}/phenotype-summary/${row.fish.zdbID}`}
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
                    showPopup={directAnnotation}
                    directCount={count.countDirect}
                    childrenCount={count.countIncludingChildren}
                    onChange={setDirectAnnotation}
                />
            )}
            <DataTable
                columns={columns}
                dataUrl={`/action/api/ontology/${termId}/phenotype?${qs.stringify(params)}`}
                onDataLoadedCount={(count) => setCount(count)}
                rowKey={row => row.fish.zdbID}
            />
        </>
    );
};

TermPhenotypeTable.propTypes = {
    termId: PropTypes.string,
    directAnnotationOnly: PropTypes.string,
};

export default TermPhenotypeTable;
