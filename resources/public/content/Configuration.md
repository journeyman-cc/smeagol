Smeagol reads a configuration file, whose content should be formatted as a clojure map.

The default content is as follows:

    {
     :site-title            "Smeagol"       ;; overall title of the site, used in page headings
     :default-locale        "en-GB"         ;; default language used for messages
     :formatters            {"vega"         smeagol.formatting/process-vega
                             "vis"          smeagol.formatting/process-vega
                             "mermaid"      smeagol.formatting/process-mermaid
                             "backticks"    smeagol.formatting/process-backticks}
    }

The three keys given above should be present. The values should be:

* **:site-title** The title for your wiki
* **:default-locale** A string comprising a lower-case [ISO 639](https://en.wikipedia.org/wiki/ISO_639) code specifying a language, optionally followed by a hyphen and an upper-case [ISO 3166](https://en.wikipedia.org/wiki/ISO_3166) specifying a country.
* **:formatters** A map of formatters used in [[Extensible Markup]], q.v.

The default file is at `resources/config.edn`; this default can be overridden by providing an environment variable, `SMEAGOL_CONFIG`, whose value is the full or relative pathname of a suitable file.
