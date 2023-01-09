import React, {useState} from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import DataTable from '../components/data-table';
import DataTableSummaryToggle from '../components/DataTableSummaryToggle';
import CommaSeparatedList from '../components/CommaSeparatedList';
import EntityLink from '../components/entity/EntityLink';

const TermGeneTable = ({termId, directAnnotationOnly}) => {

    const [directAnnotation, setDirectAnnotation] = useState(directAnnotationOnly === 'true');
    const [count, setCount] = useState({'countDirect': 0, 'countIncludingChildren': 0});

    const columns = [
        {
            label: 'Human Gene',
            content: (row) => <a href={'http://omim.org/entry/' + row.omimAccession}>{row.symbol}</a>,
            filterName: 'humanGeneName',
            width: '120px',
        },
        {
            label: 'Zebrafish Ortholog',
            content: (row) => <CommaSeparatedList>
                {row.zfinGene && (row.zfinGene.map(gene => {
                    return <EntityLink entity={gene} key={gene.zdbID} />
                }))}
            </CommaSeparatedList>,
            filterName: 'zfinGeneName',
            width: '120px',
        },
        {
            label: 'OMIM Term',
            content: (row) => <span>{row.name}</span>,
            filterName: 'omimName',
            width: '120px',
        },
        {
            label: 'Disease',
            content: (row) => <EntityLink entity={row.term} key={row.symbol}/>,
            filterName: 'termName',
            width: '120px',
        },
        {
            label: 'OMIM Phenotype ID',
            content: (row) => <a href={'http://omim.org/entry/' + row.omimNum}>{row.omimNum}</a>,
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
                    showPopup={directAnnotation}
                    directCount={count.countDirect}
                    childrenCount={count.countIncludingChildren}
                    onChange={setDirectAnnotation}
                />
            )}
            <DataTable
                columns={columns}
                dataUrl={`/action/api/ontology/${termId}/genes?${qs.stringify(params)}`}
                onDataLoadedCount={(count) => setCount(count)}
                rowKey={row => row.omimAccession}
            />
        </>
    );
};

TermGeneTable.propTypes = {
    termId: PropTypes.string,
    directAnnotationOnly: PropTypes.string,
};

export default TermGeneTable;
