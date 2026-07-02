#!/usr/bin/env groovy
/*
 * One-shot migration tool for the static-file refactor: convert zf_info JSP views
 * into static HTML. Run from the repository root.
 *
 * Each source JSP under home/WEB-INF/jsp/zf_info is:
 *     <%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
 *     <z:page title="...">
 *     ...static body...
 *     </z:page>
 * We strip the tag-import include and the <z:page> wrapper, lift the title into
 * <title>, and drop the body into the standard static scaffold (.latest webpack
 * aliases + data-zfin-chrome-* mount points + /analytics.js). The view name maps to
 * the URL/output path by replacing "--" with "/", so
 *     home/WEB-INF/jsp/zf_info/zfbook--chapt1--1.4.jsp
 *  -> static/zf_info/zfbook/chapt1/1.4.html   (served at /zf_info/zfbook/chapt1/1.4.html)
 *
 * Usage: groovy convert-zfbook-to-static.groovy [glob] [excludeGlob]
 *   glob         JSP filename glob to convert (default 'zfbook--*.jsp'; '*.jsp' = all)
 *   excludeGlob  optional filename glob to skip (e.g. 'stars.jsp')
 *
 * Note: stars.jsp is NOT mechanical (it renders a dynamic <zfin2:starRating> tag and
 * lacks a <z:page> wrapper) -- convert it by hand.
 */

def srcDir  = new File('home/WEB-INF/jsp/zf_info')
def outRoot = new File('static/zf_info')
def glob    = args.length > 0 ? args[0] : 'zfbook--*.jsp'
def exclude = args.length > 1 ? args[1] : null

def globToRegex = { String g ->
    def sb = new StringBuilder('^')
    g.each { ch ->
        if (ch == '*')      sb << '.*'
        else if (ch == '?') sb << '.'
        else if (ch ==~ /[.\\+\[\]\(\)\{\}\^\$\|]/) sb << '\\' << ch
        else                sb << ch
    }
    ~(sb << '$').toString()
}

def incRe   = ~/^\s*<%@\s*include\s+file="\/WEB-INF\/jsp-include\/tag-import\.jsp"\s*%>\s*$/
def openRe  = ~/^\s*<z:page\b[^>]*\btitle="([^"]*)"[^>]*>\s*$/
def closeRe = ~/^\s*<\/z:page>\s*$/
def esc     = { String s -> s.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;') }

def scaffold = { String title, String body -> """\
<!doctype html>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <meta http-equiv="x-ua-compatible" content="ie=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">

        <title>${title}</title>

        <link rel="stylesheet" href="/dist/style.latest.css">

        <script src="https://cdn.jsdelivr.net/npm/jquery@1.12.4/dist/jquery.min.js"></script>
        <script src="/dist/vendor-common.latest.js"></script>
        <script src="/dist/zfin-common.latest.js"></script>
        <script src="/analytics.js"></script>
    </head>
    <body>
        <div data-zfin-chrome-header></div>
        <main>
${body}
        </main>
        <div data-zfin-chrome-footer></div>
    </body>
</html>
"""
}

def globRe = globToRegex(glob)
def excludeRe = exclude ? globToRegex(exclude) : null
def sources = (srcDir.exists() ? srcDir.listFiles().toList() : [])
        .findAll { it.isFile() && it.name ==~ globRe && (!excludeRe || !(it.name ==~ excludeRe)) }
        .sort { it.name }

int written = 0, errors = 0
sources.each { jsp ->
    def lines = jsp.readLines()
    int i = lines.findIndexOf { it.trim() }                    // first non-blank
    if (i < 0 || !(lines[i] ==~ incRe)) { System.err.println "SKIP ${jsp.name}: no tag-import"; errors++; return }
    i = lines.findIndexOf(i + 1) { it.trim() }                 // next non-blank
    // NB: don't use `!m` on a Matcher -- Groovy coerces a Matcher to boolean by
    // calling find(), so `!m || !m.find()` would consume two finds. Check null
    // explicitly, then call find() exactly once.
    def m = i >= 0 ? (lines[i] =~ openRe) : null
    if (m == null || !m.find()) { System.err.println "SKIP ${jsp.name}: no <z:page title>"; errors++; return }
    def title = m.group(1)
    int close = lines.findIndexOf(i + 1) { it ==~ closeRe }
    if (close < 0) { System.err.println "SKIP ${jsp.name}: no </z:page>"; errors++; return }

    def body = lines[(i + 1)..<close].collect { it.replaceAll(/\s+$/, '') }.join('\n')
    def rel = jsp.name[0..<(jsp.name.length() - 4)].replace('--', '/') + '.html'   // strip .jsp
    def out = new File(outRoot, rel)
    out.parentFile.mkdirs()
    out.text = scaffold(esc(title), body)
    written++
}

println "${sources.size()} source JSP(s) matched '${glob}'" + (exclude ? " (excluding '${exclude}')" : "") +
        ": ${written} written" + (errors ? ", ${errors} skipped" : "")
