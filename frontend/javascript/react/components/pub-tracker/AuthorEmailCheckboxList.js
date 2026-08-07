import React from 'react';
import PropTypes from 'prop-types';
import CheckboxList from '../CheckboxList';
import {buildRecipientList} from '../../utils/publication';

const AuthorEmailCheckboxList = ({authors, id, value, onChange}) => {
    const items = buildRecipientList(authors);

    const handleChange = (selected) => {
        onChange(items.filter(item => selected.indexOf(item.email) >= 0));
    };

    return (
        <CheckboxList
            value={value.map(v => v.email)}
            items={items}
            itemIdPrefix={id}
            getItemKey={i => i.email}
            getItemDisplay={i => `${i.name} (${i.email})`}
            onChange={handleChange}
        />
    );
};

AuthorEmailCheckboxList.propTypes = {
    authors: PropTypes.array,
    id: PropTypes.string.isRequired,
    onChange: PropTypes.func,
    value: PropTypes.array,
};

export default AuthorEmailCheckboxList;
