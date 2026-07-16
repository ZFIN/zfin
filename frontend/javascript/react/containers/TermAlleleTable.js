import React, {useState} from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import DataTable from '../components/data-table';
import DataTableSummaryToggle from '../components/DataTableSummaryToggle';
import CommaSeparatedList from '../components/CommaSeparatedList';

const TermAlleleTable = ({termId, directAnnotationOnly}) => {

    const [directAnnotation, setDirectAnnotation] = useState(directAnnotationOnly === 'true');
    const [count, setCount] = useState({'countDirect': 0, 'countIncludingChildren': 0});

    const columns = [
        {
            label: 'Allele',
            content: ({allele}) => <a
                href={'/' + allele.id}
                dangerouslySetInnerHTML={{__html: allele.symbol}}
            />,
            filterName: 'alleleName',
            width: '100px',
        },
        {
            label: 'Disease',
            content: ({disease}) => <a href={'/' + disease.id}>{disease.name}</a>,
            filterName: 'disease',
            width: '120px',
        },
        {
            label: 'Evidence',
            content: (row) => <CommaSeparatedList>
                {row.evidenceCodes.map(code => {
                    return <>{code.displaySynonym}</>
                })}
            </CommaSeparatedList>,
            filterName: 'evidenceCode',
            width: '120px',
        },
        {
            label: 'Reference',
            content: (row) => <CommaSeparatedList>
                {row.publications.map(publication => {
                    return <a
                        href={publication.url}
                        dangerouslySetInnerHTML={{__html: publication.id}}
                        key={publication.id}
                    />
                })}
            </CommaSeparatedList>,
            filterName: 'reference',
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
                dataUrl={`https://stage.alliancegenome.org/api/disease/${termId}/alleles?${qs.stringify(params)}&filter.species=Danio%20rerio`}
                onDataLoadedCount={(count) => setCount(count)}
                rowKey={row => row.primaryKey}
            />
        </>
    );
};

TermAlleleTable.propTypes = {
    termId: PropTypes.string,
    directAnnotationOnly: PropTypes.string,
};

export default TermAlleleTable;
