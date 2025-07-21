import React, {useEffect} from 'react';
import {backendBaseUrl} from './DomainInfo';
import {MarkerLabelAndZdbId} from './ConstructTypes';

interface ConstructRelationshipsTableProps {
    publicationId: string;
}

//type for rows of table:
type ConstructRelationshipRow = {
    zdbID: string;
    constructZdbID: string;
    constructType: string;
    constructName: string;
    constructLabel: string;
    constructCssClass: string;
    relationshipType: string;
    markerLabel: string;
    markerZdbID: string;
}

//what the server sends back after creating a new relationship
type NewConstructRelationshipServerResponse = {
    type: string;
    zdbID: string;
    construct: {
        zdbID: string;
        name: string;
    };
    marker: {
        abbreviation: string;
        zdbID: string;
        name: string;
    };
}

const calculatedDomain = backendBaseUrl();

const ConstructRelationshipsTable = ({publicationId}: ConstructRelationshipsTableProps) => {
    const [loading, setLoading] = React.useState<boolean>(true);
    const [constructRelationshipRows, setConstructRelationshipRows] = React.useState<ConstructRelationshipRow[]>([]);
    const [publicationConstructs, setPublicationConstructs] = React.useState<MarkerLabelAndZdbId[]>([]);
    const [regionsForRelation, setRegionsForRelation] = React.useState<MarkerLabelAndZdbId[]>([]);
    const [selectedMarker, setSelectedMarker] = React.useState<string>('');
    const [selectedConstruct, setSelectedConstruct] = React.useState<string>('');
    const RELATIONSHIP_TO_ADD = 'contains region';

    function getOrderInConstruct(constructName: string, markerLabel: string) {
        return constructName.indexOf(markerLabel) === -1 ? 1000 : constructName.indexOf(markerLabel);
    }

    /**
     * Sorts the rows by constructName, then by special logic(*), then by relationshipType, then by markerLabel
     * (*) The special logic is that if the markerLabel appears in the constructName, it should appear in the order in which it appears in the constructName
     * @param constructRelationshipRows
     */
    function sortConstructRelationshipRows(constructRelationshipRows: ConstructRelationshipRow[]) {
        const sortedRows = constructRelationshipRows.sort((a, b) => {
            if (a.constructName === b.constructName) {
                const constructName = a.constructName;
                const orderInConstructA = getOrderInConstruct(constructName, a.markerLabel);
                const orderInConstructB = getOrderInConstruct(constructName, b.markerLabel);

                if (orderInConstructA === orderInConstructB) {
                    if (a.relationshipType === b.relationshipType) {
                        return a.markerLabel.localeCompare(b.markerLabel);
                    }
                    return a.relationshipType.localeCompare(b.relationshipType);
                }
                return orderInConstructA - orderInConstructB;
            }
            return a.constructName.localeCompare(b.constructName);
        });
        return styleRowsForTable(sortedRows);
    }

    /**
     * The final render of the table should omit the construct name when it repeats from row to row.
     * Each row should have a class alternating between 'evengroup' and 'oddgroup' when construct name changes.
     * The first row of each group (a group being all the same construct name) should also have 'newgroup'
     *
     * @param constructRelationshipRows
     */
    function styleRowsForTable(constructRelationshipRows: ConstructRelationshipRow[]) {
        let lastConstructName = '';
        let constructCssClass = 'oddgroup';

        return constructRelationshipRows.map( (rel) => {
            constructCssClass = constructCssClass.replace(' newgroup', '');
            if (rel.constructName === lastConstructName) {
                return {...rel, constructLabel: '', constructCssClass};
            }
            lastConstructName = rel.constructName;

            constructCssClass = constructCssClass === 'oddgroup' ? 'evengroup' : 'oddgroup';
            constructCssClass = constructCssClass + ' newgroup';

            return {...rel, constructCssClass};
        });
    }

    async function submitConstructRelationship (constructZdbID: string, markerZdbID: string, relationshipType: string, publicationZdbID: string) {
        const response = await fetch(`${calculatedDomain}/action/api/construct/${constructZdbID}/relationships`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                firstMarker: {zdbID: constructZdbID},
                secondMarker: {zdbID: markerZdbID},
                markerRelationshipType: {name: relationshipType},
                references: [{zdbID: publicationZdbID}]
            }),
        });
        return await response.json();
    }

    async function insertNewRelationshipRow(serverResponseData: NewConstructRelationshipServerResponse) {
        const {zdbID, construct, marker} = serverResponseData;

        //insert the new row at the correct position in the table (sorted by constructName, then by relationshipType, then by markerLabel)
        const newRelationshipRow: ConstructRelationshipRow = {
            zdbID,
            constructZdbID: construct.zdbID,
            constructType: '',
            constructName: construct.name,
            constructLabel: construct.name,
            constructCssClass: '',
            relationshipType: RELATIONSHIP_TO_ADD,
            markerLabel: marker.abbreviation,
            markerZdbID: marker.zdbID
        };

        const newRows = sortConstructRelationshipRows([...constructRelationshipRows, newRelationshipRow]);
        setConstructRelationshipRows(newRows);
    }

    async function removeRelationshipRow(row: ConstructRelationshipRow) {
        const newRows = constructRelationshipRows.filter((rel) => rel.zdbID !== row.zdbID);
        setConstructRelationshipRows(newRows);
    }

    async function fetchConstructRelationships() {
        setLoading(true); // Assuming you want to set loading to true at the beginning of the fetch
        try {
            const response = await fetch(`${calculatedDomain}/action/api/publication/${publicationId}/construct-relationships`);
            const constructsData = await response.json();
            const uniqueConstructsMap = {};

            const uniqueConstructs = [];

            const mappedConstructRelationships = constructsData.map(({ zdbID, constructDTO, markerDTO, relationshipType }) => {
                const { zdbID: constructZdbID, constructType: constructType, name: constructName } = constructDTO;
                const { label: markerLabel, zdbID: markerZdbID } = markerDTO;

                if (uniqueConstructsMap[constructZdbID] === undefined) {
                    uniqueConstructs.push({label: constructName, zdbID: constructZdbID});
                }
                uniqueConstructsMap[constructZdbID] = constructName;

                const constructLabel = constructName;

                return {
                    zdbID,
                    constructZdbID,
                    constructType,
                    constructName,
                    constructLabel,
                    relationshipType,
                    markerLabel,
                    markerZdbID,
                };
            });

            //sort by constructName, then by relationshipType, then by markerLabel
            const sortedMappedConstructRelationships = sortConstructRelationshipRows(mappedConstructRelationships);

            setPublicationConstructs(uniqueConstructs.sort((a, b) => a.label.localeCompare(b.label)));
            setConstructRelationshipRows(sortedMappedConstructRelationships);
        } catch (error) {
            console.error('Failed to fetch construct relationships:', error);
        } finally {
            setLoading(false);
        }
    }

    async function fetchMarkersForRelation() {
        try {
            const response = await fetch(`${calculatedDomain}/action/api/publication/${publicationId}/${RELATIONSHIP_TO_ADD}/markersForRelation`);
            const markersData = await response.json();
            const regionsData = markersData.filter(m => m.markerType === 'Engineered Region');
            setRegionsForRelation(regionsData.map(({ label, zdbID }) => ({ label, zdbID })).sort((a, b) => a.label.localeCompare(b.label)));
        } catch (error) {
            console.error('Failed to fetch markers for relation:', error);
        }
    }

    async function deleteConstructMarkerRelationship(row: ConstructRelationshipRow) {
        const {constructZdbID, zdbID} = row;
        await fetch(`${calculatedDomain}/action/api/construct/${constructZdbID}/relationships/${zdbID}`, {
            method: 'DELETE'
        });
    }

    async function handleAddButton() {
        if (selectedConstruct === '' || selectedMarker === '') {
            return;
        }
        try {
            const newRelationshipFromServer = await submitConstructRelationship(selectedConstruct, selectedMarker, RELATIONSHIP_TO_ADD, publicationId);
            insertNewRelationshipRow(newRelationshipFromServer);
        } catch (error) {
            //ignore linting rule for this alert
            //eslint-disable-next-line
            alert('Failed to add construct marker relationship');
        }
    }


    async function handleDeleteButton(rel: ConstructRelationshipRow) {
        try {
            await deleteConstructMarkerRelationship(rel);
            removeRelationshipRow(rel);
        } catch (error) {
            //ignore linting rule for this alert
            //eslint-disable-next-line
            alert('Failed to delete construct marker relationship');
        }
    }

    useEffect(() => {
        fetchConstructRelationships();
    }, [publicationId]);

    useEffect(() => {
        fetchMarkersForRelation();
    }, [publicationId]);

    if (loading) {
        return <div>Loading...</div>;
    }

    return <>
        <table className='searchresults groupstripes-hover' style={{width: '100%'}}>
            <thead/>
            <tbody>
                <tr className='table-header'>
                    <td>Construct</td>
                    <td>Type</td>
                    <td>Relationship</td>
                    <td>Target</td>
                    <td>Delete</td>
                </tr>
                {constructRelationshipRows.map(rel => (
                    <tr className={'experiment-row ' + rel.constructCssClass } key={rel.zdbID}>
                        <td>
                            <div className='gwt-HTML'>
                                {rel.constructLabel !== '' && <a href={`/${rel.constructZdbID}`} title={rel.constructLabel}>{rel.constructLabel}</a>}
                            </div>
                        </td>
                        <td>
                            <div className='gwt-Label'>{rel.constructType}</div>
                        </td>
                        <td>
                            <div className='gwt-Label'>{rel.relationshipType}</div>
                        </td>
                        <td>
                            <div className='gwt-HTML'>
                                <a href={`/${rel.markerZdbID}`} id={rel.markerZdbID} title={rel.markerLabel}>
                                    <span className='genedom' title={rel.markerLabel} id='Gene Symbol'>{rel.markerLabel}</span>
                                </a>
                            </div>
                        </td>
                        <td>
                            {rel.relationshipType === RELATIONSHIP_TO_ADD &&
                                <button type='button' className='gwt-Button' onClick={() => handleDeleteButton(rel)}>X</button>}
                        </td>
                    </tr>
                ))}
                <tr className='experiment-row'>
                    <td><select className='gwt-ListBox' name='constructToAddList' onChange={(e) => setSelectedConstruct(e.target.value)}>
                        <option value='-----------'>-----------</option>
                        {publicationConstructs.map(construct => (
                            <option key={construct.zdbID} value={construct.zdbID}>{construct.label}</option>
                        ))}
                    </select></td>
                    <td>
                        <div className='gwt-Label'/>
                    </td>
                    <td><select className='gwt-ListBox'><option>{RELATIONSHIP_TO_ADD}</option></select></td>
                    <td><select className='gwt-ListBox' onChange={(e) => setSelectedMarker(e.target.value)}>
                        <option value='-----------'>-----------</option>
                        {regionsForRelation.map(marker => (
                            <option key={marker.zdbID} value={marker.zdbID}>{marker.label}</option>
                        ))}
                    </select></td>
                </tr>
                <tr>
                    <td align='left'>
                        <button type='button' className='gwt-Button' onClick={handleAddButton}>Add</button>
                    </td>
                </tr>
            </tbody>
        </table>
    </>;
}

export default ConstructRelationshipsTable;
