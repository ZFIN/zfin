import React, { useCallback, useRef, useEffect } from 'react';
import PropTypes from 'prop-types';

const Autocompletify = ({url, onChange, typeaheadOptions, value, ...rest}) => {
    // container to hold the jQuery input element
    const $input = useRef(null);

    // when the input element mounts call the jQuery plugin and set up an
    // event listener which causes a suggestion selection to be treated
    // like a normal change event
    const refCallback = useCallback(element => {
        if (element === null) {
            return;
        }
        $input.current = $(element)
            .autocompletify(url, typeaheadOptions)
            .on('typeahead:select', (event) => {
                if (typeof onChange === 'function') {
                    onChange(event);
                }
            });
    }, [url, typeaheadOptions]);

    // if the value changes from the outside, notify the typeahead plugin
    // of the change
    useEffect(() => {
        $input.current.typeahead('val', value);
    }, [value])

    return (
        <input ref={refCallback} onChange={onChange} value={value} {...rest} />
    );
};

Autocompletify.propTypes = {
    onChange: PropTypes.func,
    typeaheadOptions: PropTypes.object,
    url: PropTypes.string,
    value: PropTypes.string
};

export default Autocompletify;
