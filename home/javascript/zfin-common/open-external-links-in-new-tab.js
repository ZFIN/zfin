// when the page loads, find all external links and set attributes: target="_blank" and rel="noopener noreferrer"
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('a.external',
            'a[href^="http://"]:not([href*="zfin.org"])',
            'a[href^="https://"]:not([href*="zfin.org"])')
        .forEach(link => {
            link.setAttribute('target', '_blank');
            link.setAttribute('rel', 'noopener noreferrer');
        });
});