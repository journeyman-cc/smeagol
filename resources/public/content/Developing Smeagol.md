## Prerequisites

You will need [Leiningen](https://github.com/technomancy/leiningen) 2.0 or above installed.

You will need [node](https://nodejs.org/en/) and [bower](https://bower.io/) installed.

## Running in development
To start a web server for the application during development, run:

    lein bower install
    lein ring server

This should start a development server, and open a new window or tab in your default browser with the default page of the wiki loaded into it.

## Editing
I generally use [LightTable](http://lighttable.com/) as my `Clojure` editor, but it doesn't really matter what you use; if you run Smeagol as described above, then all changes you make in the code (and save) will instantly be applied to the running system. This makes for a productive development environment.

## Documentation
It is my intention that the code should be sufficiently well documented to be easy to understand. Documentation may be generated from the code by running

    lein codox

## Contributing
If you make changes to Smeagol which you think are useful, please contribute them in the form of a [pull request on github](https://help.github.com/articles/creating-a-pull-request/).
