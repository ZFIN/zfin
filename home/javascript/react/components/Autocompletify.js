import React, { useCallback } from 'react';
import PropTypes from 'prop-types';

const Autocompletify = ({url, onChange, ...rest}) => {
    const refCallback = useCallback(element => {
        if (element === null) {
            return;
        }
        $(element)
            .autocompletify(url)
            .on('typeahead:select', (event) => {
                if (typeof onChange === 'function') {
                    onChange(event);
                }
            });
    }, [url]);

    return (
        <input ref={refCallback} onChange={onChange} {...rest} />
    );
};

Autocompletify.propTypes = {
    onChange: PropTypes.func,
    url: PropTypes.string,
};

export default Autocompletify;
