import React, {useState} from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import DataTable from '../components/data-table';
import DataTableSummaryToggle from '../components/DataTableSummaryToggle';
import CommaSeparatedList from '../components/CommaSeparatedList';

const PhenotypeTable = ({alleleId, directAnnotationOnly}) => {

    const [directAnnotation, setDirectAnnotation] = useState(directAnnotationOnly === 'true');
    const [count, setCount] = useState({'countDirect': 0, 'countIncludingChildren': 0});

    const columns = [
        {
            label: 'Phenotype',
            content: (row) => <>
                {row.phenotype}
            </>,
            filterName: 'termName',
            width: '120px',
        },
        {
            label: 'Fish',
            content: (row) => <CommaSeparatedList>
                {row.primaryAnnotatedEntities.map(annotation => {
                    return <a
                        href={'/' + annotation.id}
                        dangerouslySetInnerHTML={{__html: annotation.name}}
                        key={annotation.id}
                    />
                })}
            </CommaSeparatedList>,
            filterName: 'disease',
            width: '120px',
        },
/*
        {
            label: 'Reference',
            content: (row) => <CommaSeparatedList>
                {row.references.map(reference => {
                    return <a
                        href={'/' + reference.pubModID}
                        dangerouslySetInnerHTML={{__html: reference.shortCitation}}
                        key={reference.curie}
                    />
                })}
            </CommaSeparatedList>,
            filterName: 'reference',
            width: '120px',
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
                dataUrl={`https://stage.alliancegenome.org/api/allele/ZFIN:${alleleId}/phenotypes?${qs.stringify(params)}`}
                onDataLoadedCount={(count) => setCount(count)}
                rowKey={row => row.primaryKey}
            />
        </>
    );
};

PhenotypeTable.propTypes = {
    alleleId: PropTypes.string,
    directAnnotationOnly: PropTypes.string,
};

export default PhenotypeTable;
