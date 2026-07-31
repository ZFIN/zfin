import { useState } from 'react';
import { DEFAULT_TABLE_STATE } from '../components/data-table';

export default function useTableState(tableState, setTableState) {
    if ((tableState && !setTableState) || (!tableState && setTableState)) {
        if (process.env.NODE_ENV === 'development') {
            console.warn('Table state must either be controlled (by setting tableState and onTableStateChange) or uncontrolled (by setting neither)');
        }
    }

    const [controlledTableState, setControlledTableState] = useState(DEFAULT_TABLE_STATE);
    tableState = tableState || controlledTableState;
    setTableState = setTableState || setControlledTableState;

    return [tableState, setTableState];
}
