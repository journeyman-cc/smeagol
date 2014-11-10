# Welcome to Smeagol

Smeagol is a simple Git-backed Wiki inspired by [Gollum](https://github.com/gollum/gollum/wiki).

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein ring server

Alternatively, if you want to deploy to a servlet container, the simplest thing is to run:

    lein ring uberwar

(a command which I'm sure Smeagol would entirely appreciate) and deploy the resulting war file.

## TODO

The editor is at present very primitive - right back from the beginnings of the Web. It would be nice to have a rich embedded editor like [Hallo](https://github.com/bergie/hallo) or [Aloha](http://aloha-editor.org/Content.Node/index.html) but I havenven't (yet) had time to integrate them!

## License

Copyright © 2014 Simon Brooke
