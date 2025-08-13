import React, {useEffect, useState} from 'react';
import type {ConstructComponent} from './ConstructTypes';
import ConstructMarkerAutocomplete from './ConstructMarkerAutocomplete';
import {useCurateConstructEditContext} from './CurateConstructEditContext';

interface ConstructRegulatoryCodingUnitListProps {
    onChange?: (value: ConstructComponent[]) => void;
    type: string;
}


/**
 * This component is used to display a list of promoters and a text field to add new promoters.
 * The same functionality can be used for codings (hence the name of the component including Regulatory and Coding).
 * @param publicationId
 * @param onChange
 * @returns JSX Element
 * @constructor
 */
const ConstructRegulatoryCodingUnitList = ({onChange, type}: ConstructRegulatoryCodingUnitListProps) => {
    const {state, setStateByProxy} = useCurateConstructEditContext();
    const [rcUnitItems, setRcUnitItems] = useState<ConstructComponent[]>([]);
    const defaultSeparator = '-';
    const [activeTextBoxValue, setActiveTextBoxValue] = useState<ConstructComponent>(null);

    const setStagedCassette = (value) => {
        setStateByProxy(proxy => {
            proxy.stagedCassette = value;
        });
    }

    const getStagedCassette = () => {
        return state.stagedCassette;
    }

    useEffect(() => {
        if (state.selectedConstruct && state.selectedConstruct.editCassetteMode) {
            const cassette = state.selectedConstruct.cassettes[state.selectedConstruct.editCassetteIndex];
            setRcUnitItems(cassette[type]);
        }
    }, [state.selectedConstruct]);

    const styles = {
        rcUnitItems: {
            display: 'flex',
            alignItems: 'center',
            gap: '10px'
        }
    };

    const handleAutoCompleteChange = (item: ConstructComponent) => {
        const itemWithSeparator = {...item, separator: ''};
        setActiveTextBoxValue(itemWithSeparator);
        if(item.value === '') {
            setStagedCassette({...getStagedCassette(), [type]: rcUnitItems});
            onChange(rcUnitItems);
            return;
        }

        const newItems = [...rcUnitItems, itemWithSeparator];
        setStagedCassette({...getStagedCassette(), [type]: newItems});

        onChange(newItems);
    }

    const handleItemSelected = (item: ConstructComponent) => {
        const itemWithSeparator = {...item, separator: defaultSeparator};
        const newItems = [...rcUnitItems, itemWithSeparator];
        setActiveTextBoxValue(null);
        setRcUnitItemsAndNotify(newItems);
    }

    const handleItemRemoved = (itemToRemove) => {
        const newItems = rcUnitItems.filter((existingItem) => existingItem !== itemToRemove);
        setRcUnitItemsAndNotify(newItems);
    }

    const handleSeparatorChange = (index, separator) => {
        const changedPart = {...rcUnitItems[index], separator: separator};
        const newItems = [...rcUnitItems.slice(0, index), changedPart, ...rcUnitItems.slice(index + 1)];
        setRcUnitItemsAndNotify(newItems);
        if (activeTextBoxValue !== null) {
            const newItemsWithActiveTextBoxValue = [...newItems, activeTextBoxValue];
            onChange(newItemsWithActiveTextBoxValue);
        }
    }

    const setRcUnitItemsAndNotify = (items) => {
        setStagedCassette({...getStagedCassette(), [type]: items});
        setRcUnitItems(items);
        onChange(items);
    }

    return <div className='promoters' style={styles.rcUnitItems}>
        {rcUnitItems.map((part, index) => (
            <React.Fragment key={index}>
                <span className={part.id === null ? 'construct-unit-no-id' : 'construct-unit-has-id'}>{part.value}</span>
                <a href='#' onClick={(e) => {e.preventDefault(); handleItemRemoved(part)}}>
                    <i className='fa fa-trash' aria-hidden='true'/>
                    {/*&#10060;*/}
                </a>
                <select
                    className='promoter-separator'
                    value={part.separator}
                    onChange={e => handleSeparatorChange(index, e.target.value)}
                >
                    <option>-</option>
                    <option>,</option>
                    <option>.</option>
                    <option/>
                </select>
            </React.Fragment>
        ))}
        <div className='promoter-autocomplete'>
            <ConstructMarkerAutocomplete
                onSelect={handleItemSelected}
                onChangeWithObject={handleAutoCompleteChange}
            />
        </div>
    </div>;
}

export default ConstructRegulatoryCodingUnitList;