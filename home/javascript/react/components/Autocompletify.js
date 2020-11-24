import React, { useCallback, useRef, useEffect } from 'react';
import PropTypes from 'prop-types';

const Autocompletify = ({url, onChange, typeaheadOptions, value, ...rest}) => {
    // container to hold the jQuery input element
    const $input = useRef(null);

    // when the input element mounts stash the jquery object for the element
    const refCallback = useCallback(element => {
        if (element === null || $input.current !== null) {
            return;
        }
        $input.current = $(element);
    }, []);

    // if the value changes from the outside, notify the typeahead plugin
    // of the change
    useEffect(() => {
        $input.current.typeahead('val', value);
    }, [value])

    // if the url or typeahead options change, destroy any previous typeahead
    // instance on the element and setup a new one
    useEffect(() => {
        if (!$input.current) {
            return;
        }
        $input.current
            .typeahead('destroy')
            .autocompletify(url, typeaheadOptions)
            .on('typeahead:select', (event) => {
                if (typeof onChange === 'function') {
                    onChange(event);
                }
            });

    }, [url, typeaheadOptions]);

    return (
        <input ref={refCallback} onChange={onChange} value={value} autoComplete='off' {...rest} />
    );
};

Autocompletify.propTypes = {
    onChange: PropTypes.func,
    typeaheadOptions: PropTypes.object,
    url: PropTypes.string,
    value: PropTypes.string
};

export default Autocompletify;
