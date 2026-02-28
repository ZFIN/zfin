import React, { createContext, useContext, useReducer } from 'react';
import wizardReducer from './wizardReducer';
import { initialState } from './defaultValues';

const WizardContext = createContext();

export function WizardProvider({ children }) {
    const [state, dispatch] = useReducer(wizardReducer, initialState);

    const updateField = (path, value) => dispatch({ type: 'UPDATE_FIELD', path, value });

    const currentLine = () => state.lines[state.currentLineIndex];
    const currentMutation = () => currentLine()?.mutations?.[state.currentMutationIndex];

    return (
        <WizardContext.Provider value={{ state, dispatch, updateField, currentLine, currentMutation }}>
            {children}
        </WizardContext.Provider>
    );
}

export function useWizard() {
    const context = useContext(WizardContext);
    if (!context) {
        throw new Error('useWizard must be used within a WizardProvider');
    }
    return context;
}

export default WizardContext;
