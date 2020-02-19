## Prerequisites

You will need [Leiningen](https://github.com/technomancy/leiningen) 2.0 or above installed.

You will need [node](https://nodejs.org/en/) installed.

## Running in development
To start a web server for the application during development, run:

```
         lein npm install
         lein ring server
```

This should start a development server, and open a new window or tab in your default browser with the default page of the wiki loaded into it.

## Editing
I generally use [LightTable](http://lighttable.com/) as my `Clojure` editor, but it doesn't really matter what you use; if you run Smeagol as described above, then all changes you make in the code (and save) will instantly be applied to the running system. This makes for a productive development environment.

## Building for deployment

*Important:* Always run `lein clean` before building a deployment build. Once you have deployed your deployment artifact, run `lein clean` again before continuing development.

### To build a standalone executable jar file

run:

```
         lein ring uberjar
```

The resulting file may be run by invoking

```
         java -jar \[path to uberjar file\]
```

## Documentation
It is my intention that the code should be sufficiently well documented to be easy to understand. Documentation may be generated from the code by running

```
         lein codox
```

## Contributing
If you make changes to Smeagol which you think are useful, please contribute them in the form of a [pull request on github](https://help.github.com/articles/creating-a-pull-request/).
