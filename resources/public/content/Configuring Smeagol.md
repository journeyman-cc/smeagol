Smeagol's core configuration comes from a configuration file, `config.edn`, which may be overridden by [[Environment Variables]]. The default file is at `resources/config.edn`; this default can be overridden by providing an environment variable, `SMEAGOL_CONFIG`, whose value is the full or relative pathname of a suitable file.

The default configuration file is as follows:

```

{
  :content-dir          "resources/public/content"
                                        ;; where content is served from.
  :default-locale       "en-GB"         ;; default language used for messages
  :extensions-from      :local          ;; where to load JavaScript libraries
                                        ;; from: options are :local, :remote.
  :formatters                           ;; formatters for processing markdown
                                        ;; extensions.
  {:backticks    {:formatter "smeagol.formatting/process-backticks"
                  :scripts {}
                  :styles {}}
   :mermaid      {:formatter "smeagol.extensions.mermaid/process-mermaid"
                  :scripts {:core {:local "vendor/node_modules/mermaid/dist/mermaid.min.js"
                                   :remote "https://cdnjs.cloudflare.com/ajax/libs/mermaid/8.4.6/mermaid.min.js"}}}
   :pswp         {:formatter "smeagol.extensions.photoswipe/process-photoswipe"
                  :scripts {:core {:local "vendor/node_modules/photoswipe/dist/photoswipe.min.js"
                                   :remote "https://cdnjs.cloudflare.com/ajax/libs/photoswipe/4.1.3/photoswipe.min.js"}
                            :ui {:local "vendor/node_modules/photoswipe/dist/photoswipe-ui-default.min.js"
                                 :remote "https://cdnjs.cloudflare.com/ajax/libs/photoswipe/4.1.3/photoswipe-ui-default.min.js"}}
                  :styles {:core {:local "vendor/node_modules/photoswipe/dist/photoswipe.css"}
                           :skin {:local "vendor/node_modules/photoswipe/dist/default-skin/default-skin.css"}}}
   :test         {:formatter "smeagol.extensions.test/process-test" }
   :vega         {:formatter "smeagol.extensions.vega/process-vega"
                  :scripts {:core {:remote "https://cdnjs.cloudflare.com/ajax/libs/vega/5.9.1/vega.min.js"}
                            :lite {:remote "https://cdnjs.cloudflare.com/ajax/libs/vega-lite/4.1.1/vega-lite.min.js"}
                            :embed {:remote "https://cdnjs.cloudflare.com/ajax/libs/vega-embed/6.2.2/vega-embed.min.js"}}}
   :vis          {:formatter "smeagol.extensions.vega/process-vega"
                  :scripts {:core {:remote "https://cdnjs.cloudflare.com/ajax/libs/vega/5.9.1/vega.min.js"}
                            :lite {:remote "https://cdnjs.cloudflare.com/ajax/libs/vega-lite/4.1.1/vega-lite.min.js"}
                            :embed {:remote "https://cdnjs.cloudflare.com/ajax/libs/vega-embed/6.2.2/vega-embed.min.js"}}}}

  :log-level            :info           ;; the minimum logging level; one of
                                        ;; :trace :debug :info :warn :error :fatal
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

## :content-dir

The value of `content-dir` should be the full or relative path to the content to be served: the Markdown files, and the upload directories. Full paths are advised, where possible. The directory must be readable and writable by the process running Smeagol. The default is `resources/public/conten`

The value from the configuration file may be overridden with the value of the environment variable `SMEAGOL_CONTENT_DIR`.

## :default-locale

The locale which you expect the majority of your visitors will use. Content negotiation will be done of course, and the best internationalisation file available will be used, but this sets a default for users who do not have any acceptable locale known to us. The default value is `en-GB`.

This parameter may be overridden with the environment variable `SMEAGOL_DEFAULT_LOCALE`.

## :extensions-from

Where javascript support for extensions should be loaded from. Valid values are `:local` and `:remote`; if `:remote` is chosen, they will generally be loaded from [CDNJS](https://cdnjs.com/).

For an intranet site with limited outside bandwidth, or if you are particularly concerned about security, choose `:local`; otherwise, `:remote` will result in faster loading of your pages.

This parameter may be overridden with the environment variable `SMEAGOL_JS_FROM`.

## :formatters

Specifications for formatters for markup extensions.

For each extension, a map is stored with a key `:formatter`, whose value is the fully qualified name of the Clojure function providing the extension, `:scripts` and `:styles`, each of which hava one additional key for each JavaScript (in the case of `:scripts`) or CSS (in the case of `:styles`) file required by the plugin. Each of these per-file keys points to a final further map, with keys `:local` and `:remote`, whose values are URLs - relative, in the case of the `:local` key, absolute in the case of the `:remote`, pointing to where the required resource can be fetched from.

This parameter may be overridden with the environment variable `SMEAGOL_FORMATTERS`, but you'd be pretty unwise to do so unless to disable formatters altogether. Otherwise, editing the `config.edn` file would be far more convenient.

## :log-level

The level at which logging should operate. Each setting implies all of the settings more severe than itself so

1. setting `:debug` will log all of `debug, info, warn, error` and| `fatal` messages;
2. setting `:info` will log all of `info, warn, error` and| `fatal` messages;

and so on, so that setting `:fatal` will show only messages which report reasons for Smeagol to fail.

The default setting is `:info`.

This parameter may be overridden with the environment variable `SMEAGOL_LOG_LEVEL`.

## :passwd

The value of this key should be the path to the password file used to authenticate users. It should **NOT** be in the content directory! For most deployments it should be a file elsewhere in the file system, but it must be readable and writable by the account which runs the process serving Smeagol.

This parameter may be overridden with the environment variable `SMEAGOL_PASSWD`.

## :site-title

The value of this key should be the overall title of the site, which is used in page headings.

This parameter may be overridden with the environment variable `SMEAGOL_SITE_TITLE`.

## :start-page

The value of this key should be the name (without the `.md` extension) of the page to show when a user visits the base URL of your Smeagol installation.

## :thumbnails

The value of this key should be a map. Keys in this map should have values which are integers. By default, the key `:small` is bound to `64` and the key `:med` to 400. When an image file is uploaded, it is stored at the resolution you uploaded; but for every key in the `:thumbnails` map whose value is larger than the larger dimension of the uploaded file, scaled copies will also be stored in those sizes.
