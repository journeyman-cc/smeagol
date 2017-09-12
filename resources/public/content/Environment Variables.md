Smeagol can be configured entirely with environment variables. The variables are:

1. `SMEAGOL_CONFIG` (optional but advised) should be the full or relative pathname of a Smeagol [[Configuration]] file;
2. `SMEAGOL_CONTENT_DIR` should be the full or relative pathname of the directory from which Smeagol should serve content (which may initially be empty, but must be writable by the process which runs Smeagol);
3. `SMEAGOL_DEFAULT_LOCALE` which should be a locale specification in the form "en-GB", "fr-FR", or whatever to suit your users;
4. `SMEAGOL_FORMATTERS` should be an [edn](https://github.com/edn-format/edn)-formatted map of formatter directives (this would be pretty hard to do from an environment variable);
5. `SMEAGOL_LOG_LEVEL` which should be one of `TRACE DEBUG INFO WARN ERROR FATAL`
6. `SMEAGOL_PASSWD` should be the full or relative pathname of a Smeagol Passwd file - see [[Security and authentication]]. This file must contain an entry for at least your initial user, and, if you want to administer users through the user interface, must be writable by the process which runs Smeagol.
7. `SMEAGOL_SITE_TITLE` which should be the title you want shown on the header of all pages.

You can have both a configuration file and environment variables; if you do, the values of the environment variables take precedence over the values in the config file.
