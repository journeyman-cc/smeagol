Smeagol's core configuration comes from a configuration file, `config.edn`, which may be overridden by [[Environment Variables]]. The default file is at `resources/config.edn`; this default can be overridden by providing an environment variable, `SMEAGOL_CONFIG`, whose value is the full or relative pathname of a suitable file.


The default configuration file is as follows:

```

{

  :content-dir          "resources/public/content"

                                        ;; where content is served from.

  :default-locale       "en-GB"         ;; default language used for messages

  :formatters                           ;; formatters for processing markdown

                                        ;; extensions.

                        {"vega"         smeagol.formatting/process-vega

                         "vis"          smeagol.formatting/process-vega

                         "mermaid"      smeagol.extensions.mermaid/process-mermaid

                         "backticks"    smeagol.formatting/process-backticks

                         "pswp"         smeagol.formatting/process-photoswipe}

  :log-level            :info           ;; the minimum logging level; one of

                                        ;; :trace :debug :info :warn :error :fatal

  :js-from              :cdnjs          ;; where to load JavaScript libraries

                                        ;; from: options are :local, :cdnjs

  :passwd               "resources/passwd"

                                        ;; where the password file is stored

  :site-title           "Smeagol"       ;; overall title of the site, used in

                                        ;; page headings

  :start-page           "Introduction"  ;; the page shown to a visitor to the

                                        ;; root URL.

  :thumbnails           {:small 64      ;; maximum dimension of thumbnails

                                        ;; stored in the /small directory

                         :med 400       ;; maximum dimension of thumbnails

                                        ;; stored in the /med directory

                         }}

```


## content-dir

The value of `content-dir` should be the full or relative path to the content to be served: the Markdown files, and the upload directories. Full paths are advised, where possible. The directory must be readable and writable by the process running Smeagol. The default is `resources/public/conten`


The value from the configuration file may be overridden with the value of the environment variable `SMEAGOL_CONTENT_DIR`.


## default-locale

The locale which you expect the majority of your visitors will use. Content negotiation will be done of course, and the best internationalisation file available will be used, but this sets a default for users who do not have any acceptable locale known to us. The default value is `en-GB`.


This parameter may be overridden with the environment variable `SMEAGOL-DEFAULT-LOCALE`.


## formatters

Specifications for formatters for markup extensions. The exact data stored will change before Smeagol 1.1.0. TODO: update this.


## log-level

The level at which logging should operate. Each setting implies all of the settings more severe than itself so


1. setting `:debug` will log all of `debug, info, warn, error` and| `fatal` messages;

2. setting `:info` will log all of `info, warn, error` and| `fatal` messages;


and so on, so that setting `:fatal` will show only messages which report reasons for Smeagol to fail.


The default setting is `:info`.


This parameter may be overridden with the environment variable `SMEAGOL-LOG-LEVEL`.

## TODO: Complete this doumentation!
