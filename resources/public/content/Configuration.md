Smeagol reads a configuration file, whose content should be formatted as a clojure map.

The default content is as follows:

```
{
  :site-title           "Smeagol"       ;; overall title of the site, used in page headings
  :default-locale       "en-GB"         ;; default language used for messages
  :content-dir          "/usr/local/etc/content"
                                        ;; where content is served from
  :passwd               "/usr/local/etc/passwd"
                                        ;; where the password file is stored
  :log-level            :info           ;; the minimum logging level; one of
                                        ;; :trace :debug :info :warn :error :fatal
  :formatters           {"vega"         smeagol.formatting/process-vega
                         "vis"          smeagol.formatting/process-vega
                         "mermaid"      smeagol.formatting/process-mermaid
                         "backticks"    smeagol.formatting/process-backticks}
}
```

The values should be:

* `:content-dir` The directory in which your editable content is stored;
* `:default-locale` A string comprising a lower-case [ISO 639](https://en.wikipedia.org/wiki/ISO_639) code specifying a language, optionally followed by a hyphen and an upper-case [ISO 3166](https://en.wikipedia.org/wiki/ISO_3166) specifying a country.
* `:formatters` A map of formatters used in [[Extensible Markup]], q.v.
* `:log-level` The minimum level of log messages to be logged; one of `:trace :debug :info :warn :error :fatal`
* `:passwd` The path to your `passwd` file - see [[Security and authentication]];
* `:site-title` The title for your wiki.

The default file is at `resources/config.edn`; this default can be overridden by providing an environment variable, `SMEAGOL_CONFIG`, whose value is the full or relative pathname of a suitable file.

Note that all the values in the configuration can be overridden with [[Environment Variables]].
