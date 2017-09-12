## Smeagol-specific environment variables
Smeagol can be configured entirely with environment variables. The variables are:

1. `SMEAGOL_CONFIG` (optional but advised) should be the full or relative pathname of a Smeagol [[Configuration]] file;
2. `SMEAGOL_CONTENT_DIR` should be the full or relative pathname of the directory from which Smeagol should serve content (which may initially be empty, but must be writable by the process which runs Smeagol);
3. `SMEAGOL_DEFAULT_LOCALE` which should be a locale specification in the form "en-GB", "fr-FR", or whatever to suit your users;
4. `SMEAGOL_FORMATTERS` should be an [edn](https://github.com/edn-format/edn)-formatted map of formatter directives (this would be pretty hard to do from an environment variable);
5. `SMEAGOL_LOG_LEVEL` which should be one of `TRACE DEBUG INFO WARN ERROR FATAL`
6. `SMEAGOL_PASSWD` should be the full or relative pathname of a Smeagol Passwd file - see [[Security and authentication]]. This file must contain an entry for at least your initial user, and, if you want to administer users through the user interface, must be writable by the process which runs Smeagol.
7. `SMEAGOL_SITE_TITLE` which should be the title you want shown on the header of all pages.

You can have both a configuration file and environment variables; if you do, the values of the environment variables take precedence over the values in the config file.

## Other environment variables

If Smeagol is compiled as an executable jar file, the actual web server component is [Ring server](https://github.com/weavejester/ring-server). This recognises the `PORT` environment variable, and, if this is present and its value is a positive integer, will listen on the specified port (otherwise its default is 3000, which is... unusual).

Smeagol uses the [Timbre](https://github.com/ptaoussanis/timbre) logging library. This recognises the following environment variables:

1. `TIMBRE_DEFAULT_STACKTRACE_FONTS` Timbre by default colourises stacktrace dumps using ANSI terminal codes. This can be quite useful in a console, but is a real pain in a log file. To turn colourised stacktraces off, set the value of this to an empty string;
2. `TIMBRE_LEVEL` Sets the minimum logging level; but there are two problems with this. The first is that the environment variable is only read at compile time not at run time, and the second is that the syntax is a bit odd, which is why I've implemented `SMEAGOL_LOG_LEVEL` (above);
3. `TIMBRE_NS_WHITELIST` Sets a list of [Clojure namespaces](https://clojure.org/reference/namespaces) from which messages should be logged; however this is only read at compile time so isn't much use in practice;
4. `TIMBRE_NS_BLACKLIST` As above, but sets a list of namespaces from which messages should **not** be logged.