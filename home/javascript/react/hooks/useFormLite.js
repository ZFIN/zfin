/**
 * Lightweight drop-in replacement for react-form@4.0.1.
 * Provides useForm, useField, and splitFormProps with the same API surface
 * used by this codebase. Created to unblock the React 18 upgrade since
 * react-form is unmaintained and hard-pegged to React 16.
 */
import React, { useState, useCallback, useEffect, useRef, createContext, useContext, useMemo } from 'react';
import equal from 'fast-deep-equal';

// ---------------------------------------------------------------------------
// Path utilities — support dotted paths like "references.0.zdbID"
// ---------------------------------------------------------------------------
function getByPath(obj, path) {
    if (obj == null || !path) {
        return undefined;
    }
    if (!path.includes('.')) {
        return obj[path];
    }
    const keys = path.split('.');
    let current = obj;
    for (const key of keys) {
        if (current == null) {
            return undefined;
        }
        current = current[key];
    }
    return current;
}

function setByPath(obj, path, value) {
    if (!path || !path.includes('.')) {
        return { ...obj, [path]: value };
    }
    const keys = path.split('.');
    const result = Array.isArray(obj) ? [...obj] : { ...obj };
    let current = result;
    for (let i = 0; i < keys.length - 1; i++) {
        const key = keys[i];
        const nextKey = keys[i + 1];
        const isNextIndex = /^\d+$/.test(nextKey);
        if (current[key] == null) {
            current[key] = isNextIndex ? [] : {};
        } else {
            current[key] = Array.isArray(current[key])
                ? [...current[key]]
                : { ...current[key] };
        }
        current = current[key];
    }
    current[keys[keys.length - 1]] = value;
    return result;
}

// ---------------------------------------------------------------------------
// Form context — useField reads form state from this
// ---------------------------------------------------------------------------
const FormContext = createContext(null);

function useFormContext() {
    return useContext(FormContext);
}

// ---------------------------------------------------------------------------
// useForm
// ---------------------------------------------------------------------------
function useForm({ defaultValues = {}, onSubmit, validate }) {
    const [values, setValues] = useState(defaultValues != null ? { ...defaultValues } : null);
    const [meta, setMetaState] = useState({
        isSubmitting: false,
        isSubmitted: false,
        isValid: true,
        isTouched: false,
        serverError: null,
    });
    const [fieldMeta, setFieldMetaState] = useState({});
    const defaultValuesRef = useRef(defaultValues);

    // --- Field error tracking for isValid ---
    const fieldErrorsRef = useRef({});

    const setFieldError = useCallback((field, error) => {
        const next = error || null;
        if (fieldErrorsRef.current[field] === next) {
            return;
        }
        fieldErrorsRef.current = { ...fieldErrorsRef.current, [field]: next };
        const hasErrors = Object.values(fieldErrorsRef.current).some(e => !!e);
        setMetaState(prev => {
            if (prev.isValid === !hasErrors) {
                return prev;
            }
            return { ...prev, isValid: !hasErrors };
        });
    }, []);

    const unregisterFieldError = useCallback((field) => {
        const newErrors = { ...fieldErrorsRef.current };
        delete newErrors[field];
        fieldErrorsRef.current = newErrors;
        const hasErrors = Object.values(newErrors).some(e => !!e);
        setMetaState(prev => {
            if (prev.isValid === !hasErrors) {
                return prev;
            }
            return { ...prev, isValid: !hasErrors };
        });
    }, []);

    // --- Re-sync when defaultValues change (async loading) ---
    useEffect(() => {
        if (!equal(defaultValuesRef.current, defaultValues)) {
            defaultValuesRef.current = defaultValues;
            setValues(defaultValues != null ? { ...defaultValues } : null);
            setMetaState({
                isSubmitting: false,
                isSubmitted: false,
                isValid: true,
                isTouched: false,
                serverError: null,
            });
            setFieldMetaState({});
            fieldErrorsRef.current = {};
        }
    }, [defaultValues]);

    const setMeta = useCallback((updates) => {
        setMetaState(prev => ({ ...prev, ...updates }));
    }, []);

    const setFieldValue = useCallback((field, value) => {
        setValues(prev => setByPath(prev, field, value));
        setMetaState(prev => ({ ...prev, isTouched: true }));
    }, []);

    const setFieldMeta = useCallback((field, updates) => {
        setFieldMetaState(prev => ({ ...prev, [field]: { ...(prev[field] || {}), ...updates } }));
    }, []);

    const getFieldValue = useCallback((field) => {
        return getByPath(values, field);
    }, [values]);

    const pushFieldValue = useCallback((field, value = '') => {
        setValues(prev => {
            const arr = Array.isArray(prev[field]) ? prev[field] : [];
            return { ...prev, [field]: [...arr, value] };
        });
        setMetaState(prev => ({ ...prev, isTouched: true }));
    }, []);

    const removeFieldValue = useCallback((field, index) => {
        setValues(prev => {
            const arr = Array.isArray(prev[field]) ? prev[field] : [];
            return { ...prev, [field]: [...arr.slice(0, index), ...arr.slice(index + 1)] };
        });
        setMetaState(prev => ({ ...prev, isTouched: true }));
    }, []);

    const reset = useCallback(() => {
        const dv = defaultValuesRef.current;
        setValues(dv != null ? { ...dv } : null);
        setMetaState({
            isSubmitting: false,
            isSubmitted: false,
            isValid: true,
            isTouched: false,
            serverError: null,
        });
        setFieldMetaState({});
        fieldErrorsRef.current = {};
    }, []);

    const handleSubmit = useCallback(async (e) => {
        if (e && e.preventDefault) {
            e.preventDefault();
        }
        if (!onSubmit) {
            return;
        }
        // Run form-level validation
        if (validate) {
            const formError = validate(values);
            if (formError && typeof formError === 'string') {
                setMetaState(prev => ({ ...prev, serverError: formError }));
                return;
            }
        }
        setMetaState(prev => ({ ...prev, isSubmitting: true, serverError: null }));
        try {
            await onSubmit(values);
            setMetaState(prev => ({
                ...prev,
                isSubmitting: false,
                isSubmitted: true,
            }));
        // TODO(ZFIN-9922): drop unused catch param or surface the error properly
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        } catch (error) {
            setMetaState(prev => ({
                ...prev,
                isSubmitting: false,
                isSubmitted: true,
            }));
        }
    }, [onSubmit, validate, values]);

    const formContext = useMemo(() => ({
        values,
        setFieldValue,
        fieldMeta,
        setFieldMeta,
        setFieldError,
        unregisterFieldError,
    }), [values, setFieldValue, fieldMeta, setFieldMeta, setFieldError, unregisterFieldError]);

    // Keep refs so the stable Form component always reads the latest values
    const formContextRef = useRef(formContext);
    formContextRef.current = formContext;
    const handleSubmitRef = useRef(handleSubmit);
    handleSubmitRef.current = handleSubmit;

    // Form component wrapper — stable identity to avoid remounting the subtree
    const [Form] = useState(() =>
        function Form({ children, ...props }) {
            return React.createElement(
                FormContext.Provider,
                { value: formContextRef.current },
                React.createElement('form', {
                    onSubmit: (e) => handleSubmitRef.current(e),
                    ...props,
                }, children)
            );
        }
    );

    return {
        Form,
        values,
        setValues,
        meta,
        setMeta,
        setFieldValue,
        setFieldMeta,
        getFieldValue,
        pushFieldValue,
        removeFieldValue,
        reset,
    };
}

// ---------------------------------------------------------------------------
// useField
// ---------------------------------------------------------------------------
function useField(field, fieldOptions = {}) {
    const formContext = useFormContext();

    if (!formContext) {
        throw new Error('useField must be used inside a <Form> component');
    }

    const { values, setFieldValue, fieldMeta, setFieldError, unregisterFieldError } = formContext;
    const value = getByPath(values, field);
    const myMeta = fieldMeta[field] || {};
    const [touched, setTouched] = useState(false);
    const [error, setErrorState] = useState(null);
    const validateRef = useRef(fieldOptions.validate);
    validateRef.current = fieldOptions.validate;
    const debounceTimerRef = useRef(null);

    // Wrap setError to bubble up to form-level isValid tracking
    const setError = useCallback((err) => {
        const normalized = err || null;
        setErrorState(normalized);
        if (setFieldError) {
            setFieldError(field, normalized);
        }
    }, [field, setFieldError]);

    // Cleanup: unregister field error and cancel debounce on unmount
    useEffect(() => {
        return () => {
            if (debounceTimerRef.current) {
                clearTimeout(debounceTimerRef.current);
            }
            if (unregisterFieldError) {
                unregisterFieldError(field);
            }
        };
    }, [field, unregisterFieldError]);

    const setValue = useCallback((newValue) => {
        setFieldValue(field, newValue);
        setTouched(true);
        // Run validation if provided
        if (validateRef.current) {
            // Cancel any pending debounced validation
            if (debounceTimerRef.current) {
                clearTimeout(debounceTimerRef.current);
                debounceTimerRef.current = null;
            }

            const debounce = (fn, ms) => {
                return new Promise((resolve) => {
                    debounceTimerRef.current = setTimeout(() => {
                        debounceTimerRef.current = null;
                        Promise.resolve(fn()).then(resolve).catch(() => resolve(null));
                    }, ms || 0);
                });
            };

            const result = validateRef.current(newValue, { debounce });
            if (result && typeof result.then === 'function') {
                result.then(r => setError(r || null));
            } else if (typeof result === 'string') {
                setError(result);
            } else if (result !== undefined) {
                setError(null);
            }
            // If result is undefined (debounce was called), the promise handles it
        }
    }, [field, setFieldValue, setError]);

    const getInputProps = useCallback((additionalProps = {}) => {
        const { ref, ...restAdditional } = additionalProps;
        return {
            name: field,
            value: value !== undefined && value !== null ? value : '',
            onChange: (e) => {
                const newValue = e.target.type === 'checkbox' ? e.target.checked : e.target.value;
                setValue(newValue);
            },
            onBlur: () => setTouched(true),
            ref,
            ...restAdditional,
        };
    }, [field, value, setValue]);

    return {
        value,
        meta: {
            error: myMeta.error || error,
            isTouched: touched,
        },
        getInputProps,
        setValue,
    };
}

// ---------------------------------------------------------------------------
// splitFormProps — separates field-related props from the rest
// ---------------------------------------------------------------------------
const FORM_PROP_KEYS = [
    'field', 'defaultValue', 'defaultIsTouched', 'defaultError',
    'defaultMeta', 'validatePristine', 'validate', 'onSubmit',
    'defaultValues', 'filterValue', 'debugForm'
];

function splitFormProps(props) {
    const fieldOptions = {};
    const rest = {};
    let field = undefined;

    for (const [key, val] of Object.entries(props)) {
        if (key === 'field') {
            field = val;
        } else if (FORM_PROP_KEYS.includes(key)) {
            fieldOptions[key] = val;
        } else {
            rest[key] = val;
        }
    }

    return [field, fieldOptions, rest];
}

export { useForm, useField, splitFormProps, useFormContext };
