import React, { useEffect, useRef, useState, KeyboardEvent } from 'react';
import {ConstructComponent} from './ConstructTypes';
import {backendBaseUrl} from './DomainInfo';

/**
 * This is a React component that is used to allow the user to start typing
 * and get autocomplete suggestions for markers.
 *
 * Endpoint example: /action/construct/find-constructMarkers?term=ae&pub=ZDB-PUB-220103-2
 *
 * Response example: [
 *     {
 *         "id": null,
 *         "name": null,
 *         "label": "Kaede (EFG)",
 *         "value": "Kaede",
 *         "url": null,
 *         "category": null
 *     },
 *     {
 *         "id": null,
 *         "name": null,
 *         "label": "TAEL (EREGION)",
 *         "value": "TAEL",
 *         "url": null,
 *         "category": null
 *     },
 *     {
 *         "id": null,
 *         "name": null,
 *         "label": "Tae. (Triticum aestivum)",
 *         "value": "Tae.",
 *         "url": null,
 *         "category": null
 *     }
 * ]
 *
 * @constructor
 */


interface ConstructMarkerAutocompleteProps {
    publicationId: string;
    resetFlag?: number;
    onSelect: (suggestion: ConstructComponent) => void;
    onChange?: (value: string) => void;
    onChangeWithObject?: (suggestion: ConstructComponent) => void;
}

const calculatedDomain = backendBaseUrl();

function ConstructMarkerAutocomplete({publicationId, resetFlag, onSelect, onChange, onChangeWithObject}: ConstructMarkerAutocompleteProps) {
    const [input, setInput] = useState<string>('');
    const [suggestions, setSuggestions] = useState<ConstructComponent[]>([]);
    const [selectedIndex, setSelectedIndex] = useState<number>(-1);
    const dropdownRef = useRef<HTMLUListElement>(null);

    const resetState = () => {
        setInput('');
        setSuggestions([]);
        setSelectedIndex(-1);
    }

    useEffect(() => {
        if (input.length > 1) {
            fetch(`${calculatedDomain}/action/construct/find-constructMarkers?term=${input}&pub=${publicationId}`)
                .then(response => response.json())
                .then(data => setSuggestions(data))
                .catch(error => console.error('Error fetching data:', error));
        } else {
            setSuggestions([]);
        }
    }, [input, publicationId]);

    useEffect(() => {
        resetState();
    }, [resetFlag]);

    // Handle keyboard navigation
    const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'ArrowDown') {
            setSelectedIndex((prevIndex) => (prevIndex < suggestions.length - 1 ? prevIndex + 1 : prevIndex));
        } else if (e.key === 'ArrowUp') {
            setSelectedIndex((prevIndex) => (prevIndex > 0 ? prevIndex - 1 : 0));
        } else if (e.key === 'Enter' ) {
            const selectedSuggestion = suggestions[selectedIndex];
            if (selectedSuggestion && selectedIndex >= 0) {
                handleSelection(selectedSuggestion);
            } else {
                handleFreeTextAdded();
            }
        } else if (e.key === 'Escape') {
            setSuggestions([]);
        } else if (e.key === 'Tab') {
            setSuggestions([]);
        }
    };

    // Handle item selection
    const handleSelection = (suggestion: ConstructComponent) => {
        handleChange(suggestion.value);
        setSuggestions([]);
        setInput('');
        setSelectedIndex(-1);
        onSelect(suggestion);
    };

    const handleChange = (value: string) => {
        setInput(value);
        if (onChange) {
            onChange(value);
        }
        if (onChangeWithObject) {
            const suggestion: ConstructComponent = {
                id: null,
                name: null,
                label: value,
                value: value,
                url: null,
                category: null,
                separator: '',
                type: ''
            };
            onChangeWithObject(suggestion);
        }
    }

    const handleFreeTextAdded = () => {
        if (input == null || input.trim().length === 0) {
            return;
        }
        const suggestion: ConstructComponent = {
            id: null,
            name: null,
            label: input,
            value: input,
            url: null,
            category: null,
            separator: '',
            type: ''
        };
        handleSelection(suggestion);
    }

    const shouldDisableAddButton = (): string => {
        const shouldDisable = input == null || input.trim().length === 0;
        return shouldDisable ? 'disabled' : '';
    }


    // Hide dropdown when clicking outside
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setSuggestions([]);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, [dropdownRef]);

    const dropdownStyle = {
        position: 'absolute',
        zIndex: 1,
        backgroundColor: 'white',
        listStyleType: 'none',
        padding: 0,
        marginTop: 0,
        border: '2px solid #999',
        borderTop: '1px solid #999',
        borderRadius: '0 0 5px 5px',
    };

    const dropdownItemStyle = {
        padding: '5px',
        cursor: 'pointer',
    };

    return (
        <>
            <input
                type='text'
                value={input}
                onChange={(e) => handleChange(e.target.value)}
                onKeyDown={handleKeyDown}
            />
            {suggestions.length > 0 && (
                <ul ref={dropdownRef} style={dropdownStyle}>
                    {suggestions.map((suggestion, index) => (
                        <li
                            key={index}
                            style={{...dropdownItemStyle, backgroundColor: selectedIndex === index ? 'lightgrey' : 'transparent'}}
                            onClick={() => handleSelection(suggestion)}
                            onMouseEnter={() => setSelectedIndex(index)}
                        >
                            {suggestion.label}
                        </li>
                    ))}
                </ul>
            )}
            <button onClick={handleFreeTextAdded} disabled={shouldDisableAddButton()}>+</button>
        </>
    );
}


export default ConstructMarkerAutocomplete;
export type { ConstructComponent };
