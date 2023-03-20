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
            content: (row) => <a href={'http://omim.org/entry/' + row.homoSapiensGene.id}>{row.homoSapiensGene.symbol}</a>,
            filterName: 'humanGeneName',
            width: '120px',
        },
        {
            label: 'Zebrafish Ortholog',
            content: (row) => <CommaSeparatedList>
                {row.zfinGene && (row.zfinGene.map(gene => {
                    return <EntityLink entity={gene} key={gene.zdbID}/>
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
            content: (row) => (row.disease && <a href={'/' + row.disease.oboID}>{row.disease.termName}</a>),
            filterName: 'termName',
            width: '120px',
        },
        {
            label: 'OMIM Phenotype ID',
            content: (row) => <a href={'http://omim.org/entry/' + row.omimAccession}>{row.omimAccession}</a>,
            filterName: 'omimAccessionID',
            width: '120px',
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
                dataUrl={`/action/api/ontology/${termId}/genes?${qs.stringify(params)}`}
                onDataLoadedCount={(count) => setCount(count)}
                rowKey={() => Math.random()}
            />
        </>
    );
};

TermGeneTable.propTypes = {
    termId: PropTypes.string,
    directAnnotationOnly: PropTypes.string,
};

export default TermGeneTable;
