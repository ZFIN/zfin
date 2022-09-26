import React, {useState} from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import DataTable from '../components/data-table';
import DataTableSummaryToggle from '../components/DataTableSummaryToggle';

const TermZebrafishModelTable = ({termId, directAnnotationOnly}) => {

    const [directAnnotation, setDirectAnnotation] = useState(directAnnotationOnly === 'true');
    const [count, setCount] = useState({'countDirect': 0, 'countIncludingChildren': 0});

    const columns = [
        {
            label: 'Fish',
            content: (row) => <a
                href={'/' + row.fishModel.fish.zdbID}
                dangerouslySetInnerHTML={{__html: row.fishModel.fish.name}}
            />,
            width: '120px',
        },
        /*
                {
                    label: 'Phenotype',
                    content: ({phenotypeObserved}) => <CommaSeparatedList>
                        {phenotypeObserved.map(entity => {
                            return <PhenotypeStatementLink key={entity.id} entity={entity}/>
                        })}
                    </CommaSeparatedList>,
                    width: '220px',
                },
        */
        /*
                {
                    label: 'Term',
                    content: ({anatomyItem}) => <EntityLink entity={anatomyItem}/>,
                    width: '120px',
                },
        */
        /*
                {
                    label: 'Citation',
                    content: row => (
                        <FigureSummary
                            statistics={row}
                            allFiguresUrl={`/action/ontology/${row.anatomyItem.zdbID}/phenotype-summary/${row.fish.zdbID}`}
                        />
                    ),
                    width: '100px',
                },
        */

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
                dataUrl={`/action/api/ontology/${termId}/zebrafish-models?${qs.stringify(params)}`}
                onDataLoadedCount={(count) => setCount(count)}
                rowKey={row => row.fishModel.zdbID}
            />
        </>
    );
};

TermZebrafishModelTable.propTypes = {
    termId: PropTypes.string,
    directAnnotationOnly: PropTypes.string,
};

export default TermZebrafishModelTable;
