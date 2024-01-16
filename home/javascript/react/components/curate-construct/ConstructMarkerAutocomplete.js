import React, {useEffect, useRef, useState} from 'react';
import PropTypes from 'prop-types';


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
const DOMAIN = 'https://cell-mac.zfin.org';

const ConstructMarkerAutocomplete = ({ publicationId, resetFlag, onSelect, onChange }) => {
    const [input, setInput] = useState('');
    const [suggestions, setSuggestions] = useState([]);
    const [selectedIndex, setSelectedIndex] = useState(-1);
    const [selectedSuggestion, setSelectedSuggestion] = useState(null);
    const [reset, setReset] = useState(resetFlag);
    const dropdownRef = useRef(null);

    useEffect(() => {
        if (input.length > 1) {
            fetch(`${DOMAIN}/action/construct/find-constructMarkers?term=${input}&pub=${publicationId}`)
                .then(response => response.json())
                .then(data => setSuggestions(data))
                .catch(error => console.error('Error fetching data:', error));
        } else {
            setSuggestions([]);
        }
    }, [input, publicationId]);

    useEffect(() => {
        if (resetFlag !== reset) {
            setReset(resetFlag);
            setInput('');
            setSuggestions([]);
        }
    }, [resetFlag]);

    // Handle keyboard navigation
    const handleKeyDown = (e) => {
        console.log('handleKeyDown', e.key, selectedIndex, suggestions.length);
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
    const handleSelection = (suggestion) => {
        setSelectedSuggestion(suggestion);
        handleChange(suggestion.value);
        setSuggestions([]);
        setInput('');
        setSelectedIndex(-1);
        onSelect(suggestion);
    };

    const handleChange = (value) => {
        setInput(value);
        if (onChange) {
            onChange(value);
        }
    }

    const handleFreeTextAdded = () => {
        if (input == null || input.trim().length === 0) {
            return;
        }
        const suggestion = {
            id: null,
            name: null,
            label: input,
            value: input,
            url: null,
            category: null
        };
        handleSelection(suggestion);
    }

    const shouldDisableAddButton = () => {
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

    return (
        <>
            <input
                type="text"
                value={input}
                onChange={(e) => handleChange(e.target.value)}
                onKeyDown={handleKeyDown}
            />
            {suggestions.length > 0 && (
                <ul ref={dropdownRef} style={{ position: 'absolute', zIndex: 1, backgroundColor: 'white', listStyleType: 'none', padding: 0, marginTop: 0 }}>
                    {suggestions.map((suggestion, index) => (
                        <li
                            key={index}
                            style={{ padding: '5px', cursor: 'pointer', backgroundColor: selectedIndex === index ? 'lightgrey' : 'transparent' }}
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
};

ConstructMarkerAutocomplete.propTypes = {
    publicationId: PropTypes.string.isRequired,
    onSelect: PropTypes.func,
    onChange: PropTypes.func,
};

export default ConstructMarkerAutocomplete;
