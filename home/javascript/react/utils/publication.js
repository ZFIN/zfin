export const splitEmailRecipientListString = (list) => (
    list.split(/[;,\s]+/).filter(v => !!v)
);

export const buildRecipientList = (authors) => {
    const recipients = [];
    authors.forEach(author => {
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
