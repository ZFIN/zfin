export const splitEmailRecipientListString = (list) => (
    list.split(/[;,\s]+/).filter(v => !!v)
);

export const buildRecipientList = (authors) => {
    const recipients = [];
    authors.forEach(author => {
        // TODO(ZFIN-9922): rewrite as `if (author.email) { ... }` -- short-circuit
        // expression-statements are flagged by typescript-eslint 8.x's no-unused-expressions
        // eslint-disable-next-line @typescript-eslint/no-unused-expressions
        author.email && splitEmailRecipientListString(author.email).forEach(email => {
            if (!email) { return; }
            recipients.push({
                email,
                name: author.fullName,
            });
        });
    });
    return recipients;
};
