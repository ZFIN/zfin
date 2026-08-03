import React, {useState} from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import DataTable from '../components/data-table';
import DataTableSummaryToggle from '../components/DataTableSummaryToggle';
import CommaSeparatedList from '../components/CommaSeparatedList';
import PhenotypeStatementLink from '../components/entity/PhenotypeStatementLink';
import FigureSummaryPhenotype from '../components/FigureSummaryPhenotype';
import ShowDevInfo from '../components/ShowDevInfo';

const ChebiPhenotypeTable = ({termId, directAnnotationOnly, endpointUrl = 'phenotype-chebi', isWildtype, isMultiChebiCondition, showDevInfo = false, indexer, hasChebiInPhenotype = false}) => {

    const [directAnnotation, setDirectAnnotation] = useState(directAnnotationOnly === 'true');
    const [count, setCount] = useState({'countDirect': 0, 'countIncludingChildren': 0});

    const columns = [
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
            label: 'Conditions',
            content: (row) => <span className='text-break'>
                <a
                    className='text-break'
                    href={`/${row.experiment.zdbID}`}
                    dangerouslySetInnerHTML={{__html: row.experiment.conditions}}
                />
                <a
                    className='popup-link data-popup-link'
                    href={`/action/experiment/popup/${row.experiment.zdbID}`}
                />
            </span>,
            filterName: 'conditionName',
            width: '300px',
        },
        {
            label: 'Phenotype',
            content: ({phenotypeStatements}) => <CommaSeparatedList>
                {phenotypeStatements.map(entity => {
                    return <PhenotypeStatementLink key={entity.id} entity={entity}/>
                })}
            </CommaSeparatedList>,
            filterName: 'phenotype',
            width: '300px',
        },
        {
            label: 'Term',
            content: ({allChebiTerms}) => <CommaSeparatedList>
                {allChebiTerms.map(term => {
                    return <a href={'/' + term.zdbID} key={Math.random()}>{term.termName}</a>
                })}
            </CommaSeparatedList>,
            filterName: 'termName',
            width: '120px',
        },
        {
            label: 'Figures',
            content: row => (
                <FigureSummaryPhenotype
                    statistics={row}
                    allFiguresUrl={`/action/ontology/${row.term.zdbID}/chebi-phenotype-summary/${row.experiment.zdbID}`}
                />
            ),
            width: '100px',
        },

    ];

    const params = {};
    if (directAnnotation) {
        params.directAnnotation = true;
    }
    params.hasChebiInPhenotype = hasChebiInPhenotype;
    if (isWildtype) {
        params.isWildtype = isWildtype;
    }
    if (isMultiChebiCondition) {
        params.isMultiChebiCondition = isMultiChebiCondition;
    }
    return (
        <>
            <ShowDevInfo
                show={showDevInfo}
                url={`/action/api/ontology/${termId}/${endpointUrl}?${qs.stringify(params)}`}
                indexer={indexer}
            />

            {directAnnotationOnly && count.countIncludingChildren > 0 && (
                <DataTableSummaryToggle
                    showPopup={directAnnotation}
                    directCount={count.countDirect}
                    childrenCount={count.countIncludingChildren}
                    onChange={setDirectAnnotation}
                    url={`/action/api/ontology/${termId}/${endpointUrl}?${qs.stringify(params)}`}
                    show={showDevInfo}
                />
            )}
            <DataTable
                columns={columns}
                dataUrl={`/action/api/ontology/${termId}/${endpointUrl}?${qs.stringify(params)}`}
                onDataLoadedCount={(count) => setCount(count)}
                rowKey={row => row.fish.zdbID + row.experiment.zdbID}
            />
        </>
    );
};

ChebiPhenotypeTable.propTypes = {
    termId: PropTypes.string,
    indexer: PropTypes.string,
    endpointUrl: PropTypes.string,
    isWildtype: PropTypes.bool,
    hasChebiInPhenotype: PropTypes.bool,
    showDevInfo: PropTypes.bool,
    isMultiChebiCondition: PropTypes.bool,
    directAnnotationOnly: PropTypes.string,
};

export default ChebiPhenotypeTable;
