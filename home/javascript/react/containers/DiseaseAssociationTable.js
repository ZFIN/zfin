import React, {useState} from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import DataTable from '../components/data-table';
import DataTableSummaryToggle from '../components/DataTableSummaryToggle';
import CommaSeparatedList from '../components/CommaSeparatedList';

const DiseaseAssociationTable = ({alleleId, directAnnotationOnly}) => {

    const [directAnnotation, setDirectAnnotation] = useState(directAnnotationOnly === 'true');
    const [count, setCount] = useState({'countDirect': 0, 'countIncludingChildren': 0});

    const columns = [
        {
            label: 'Disease',
            content: ({object}) => <a href={'/' + object.curie}>{object.name}</a>,
            filterName: 'disease',
            width: '120px',
        },
        {
            label: 'Evidence',
            content: (evidenceCodes) => <CommaSeparatedList>
                {evidenceCodes.evidenceCodes.map(code => {
                    return <>{code.abbreviation}</>
                })}
            </CommaSeparatedList>,
            filterName: 'evidenceCode',
            width: '120px',
        },
        {
            label: 'Reference',
            content: (row) => <CommaSeparatedList>
                {row.references.map(reference => {
                    return <a
                        href={reference.curie}
                        dangerouslySetInnerHTML={{__html: reference.shortCitation}}
                        key={reference.curie}
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
                dataUrl={`https://stage.alliancegenome.org/api/allele/ZFIN:${alleleId}/diseases?${qs.stringify(params)}`}
                onDataLoadedCount={(count) => setCount(count)}
                rowKey={row => row.primaryKey}
            />
        </>
    );
};

DiseaseAssociationTable.propTypes = {
    alleleId: PropTypes.string,
    directAnnotationOnly: PropTypes.string,
};

export default DiseaseAssociationTable;
