# Welcome to Smeagol!

Smeagol is a simple Wiki engine inspired by [Gollum](https://github.com/gollum/gollum/wiki). Gollum is a Wiki engine written in Ruby, which uses a number of simple text formats including [Markdown](http://daringfireball.net/projects/markdown/), which uses [Git](http://git-scm.com/) to provide versioning and backup. I needed a new Wiki for a project and thought Gollum would be ideal - but unfortunately it doesn't provide user authentication, which I needed, and it was simpler for me to reimplement the bits I did need in Clojure than to modify Gollum.

So at this stage Smeagol is a Wiki engine written in Clojure which uses Markdown as its text format, which does have user authentication, and which uses Git as its versioning and backup system.

## DLL Hell

Unfortunately, Smeagol currently uses **[clj-jgit](https://github.com/clj-jgit/clj-jgit)** for Git integration, and **clj-jgit** depends on an obsolete version (0.5.3) of **org.clojure/core.memoize**, whereas other components of the system rely on the current (0.5.6) version. This conflict currently makes it tricky to do an out-of-the-box build of Smeagol. You need to check out clj-jgit and build a custom build, depending on the current, not the obsolete, version of memoize. I'm working to fix this problem.

## Markup syntax

Smeagol uses the Markdown format as provided by [markdown-clj](https://github.com/yogthos/markdown-clj), with the addition that anything enclosed in double square brackets, \[\[like this\]\], will be treated as a link into the wiki.

## Security and authentication

Currently security is very weak. There is currently a file called *passwd* in the *resources/public* directory, which contains a clojure map of which maps username to maps with plain-text passwords and emails thus:

    {:admin {:password "admin" :email "admin@localhost"}
     :adam {:password "secret" :email "adam@localhost"}}

that is to say, the username is a keyword and the corresponding password is a string. Obviously, this is a temporary solution while in development which I will fix later.

## Todo

* Image (and other media) upload;
* Improved editor. The editor is at present very primitive - right back from the beginnings of the Web. It would be nice to have a rich embedded editor like [Hallo](https://github.com/bergie/hallo) or [Aloha](http://aloha-editor.org/Content.Node/index.html) but I havenven't (yet) had time to integrate them!
* Improved security. Having the passwords in plain text rather than encrypted is just basically poor; having the passwd file in *public* space is also poor (although I believe it cannot be accessed via HTTP). Essentially, authentication mechanisms should be pluggable, and at present they aren't;
* Mechanism to add users through the user interface;
* Mechanism to change passwords through the user interface;

## License

Copyright © 2014 Simon Brooke. Licensed under the GNU General Public License,
version 2.0 or (at your option) any later version.

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein ring server

Alternatively, if you want to deploy to a servlet container, the simplest thing is to run:

    lein ring uberwar

(a command which I'm sure Smeagol would entirely appreciate) and deploy the resulting war file.
