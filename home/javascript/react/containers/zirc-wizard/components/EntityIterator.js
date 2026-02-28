import React from 'react';
import PropTypes from 'prop-types';

const EntityIterator = ({ items, currentIndex, onSelectIndex, label, renderItem }) => {
    if (!items || items.length <= 1) {
        return renderItem(items?.[0] || {}, 0);
    }

    return (
        <div>
            <ul className='nav nav-tabs mb-3'>
                {items.map((_, i) => (
                    <li className='nav-item' key={i}>
                        <button
                            type='button'
                            className={`nav-link ${i === currentIndex ? 'active' : ''}`}
                            onClick={() => onSelectIndex(i)}
                        >
                            {label} {i + 1}
                        </button>
                    </li>
                ))}
            </ul>
            {renderItem(items[currentIndex], currentIndex)}
        </div>
    );
};

EntityIterator.propTypes = {
    items: PropTypes.array.isRequired,
    currentIndex: PropTypes.number.isRequired,
    onSelectIndex: PropTypes.func.isRequired,
    label: PropTypes.string.isRequired,
    renderItem: PropTypes.func.isRequired,
};

export default EntityIterator;
