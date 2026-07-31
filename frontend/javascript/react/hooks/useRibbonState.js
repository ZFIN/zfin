import { useState } from 'react';

export default function useRibbonState() {
    const [selected, setSelected] = useState(null);

    const handleItemClick = (subject, group) => {
        if (!subject || !group || (selected && selected.group.id === group.id && selected.group.type === group.type)) {
            setSelected(null);
        } else {
            setSelected({subject, group});
        }
    };

    return [selected, handleItemClick];
}
