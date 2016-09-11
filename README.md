# Welcome to Smeagol!

![One wiki to rule them all](https://www.weft.scot/images/smeagol.png)

# Welcome to Smeagol!
Smeagol is a simple Wiki engine inspired by [Gollum](https://github.com/gollum/gollum/wiki). Gollum is a Wiki engine written in Ruby, which uses a number of simple text formats including [Markdown](http://daringfireball.net/projects/markdown/), and which uses [Git](http://git-scm.com/) to provide versioning and backup. I needed a new Wiki for a project and thought Gollum would be ideal - but unfortunately it doesn't provide user authentication, which I needed, and it was simpler for me to reimplement the bits I did need in Clojure than to modify Gollum.

So at this stage Smeagol is a Wiki engine written in Clojure which uses Markdown as its text format, which does have user authentication, and which uses Git as its versioning and backup system.

## Status
Smeagol is now a fully working small Wiki engine, and meets my own immediate needs. There are some obvious
things which could be improved - see **TODO** list below - but it works now and doesn't seem to have any major problems.

## Markup syntax
Smeagol uses the Markdown format as provided by [markdown-clj](https://github.com/yogthos/markdown-clj), with the addition that anything enclosed in double square brackets, \[\[like this\]\], will be treated as a link into the wiki itself.

## Security and authentication
Security is now greatly improved. There is a file called *passwd* in the *resources* directory, which contains a clojure map which maps usernames to maps with plain-text passwords and emails thus:

    {:admin {:password "admin" :email "admin@localhost" :admin true}
     :adam {:password "secret" :email "adam@localhost"}}

that is to say, the username is a keyword and the corresponding password is a string. However, since version 0.5.0, users can now change their own passwords, and when the user changes their password their new password is encrypted using the [scrypt](http://www.tarsnap.com/scrypt.html) one-way encryption scheme. The password file is now no longer either in the *resources/public* directory so cannot be downloaded through the browser, nor in the git archive to which the Wiki content is stored, so that even if that git archive is remotely clonable an attacker cannot get the password file that way.

## Images
Smeagol does not currently have any mechanism to upload images. You can, however, link to images already available on the web, like this:

![Smeagol](http://vignette3.wikia.nocookie.net/lotr/images/e/e1/Gollum_Render.png/revision/latest?cb=20141218075509)

## Todo
* Mechanism to add users through the user interface;

## Advertisement
If you like what you see here, I am available for work on open source Clojure projects. Contact me vis [WEFT](http://www.weft.scot/).

### Phoning home
Smeagol currently requests the WEFT logo in the page footer from my home site. This is mainly so I can get a feel for how many people are using the product. If you object to this, edit the file

    resources/templates/base.html

and replace the line

    <img height="16" width="16" alt="The Web Engineering Factory &amp; Toolworks" src="http://www.weft.scot/images/weft.logo.64.png"> Developed by <a href="http://www.weft.scot/">WEFT</a>

with the line

    <img height="16" width="16" alt="The Web Engineering Factory &amp; Toolworks" src="img/weft.logo.64.png"> Developed by <a href="http://www.weft.scot/">WEFT</a>

## License
Copyright © 2014-2015 Simon Brooke. Licensed under the GNU General Public License,
version 2.0 or (at your option) any later version. If you wish to incorporate
parts of Smeagol into another open source project which uses a less restrictive
license, please contact me; I'm open to dual licensing it.

## Prerequisites
You will need [Leiningen](https://github.com/technomancy/leiningen) 2.0 or above installed.

You will need [node](https://nodejs.org/en/) and [bower](https://bower.io/) installed.

## Running
To start a web server for the application, run:

    lein bower install
    lein ring server

Alternatively, if you want to deploy to a servlet container (which I would strongly recommend), the simplest thing is to run:

    lein bower install
    lein ring uberwar

(a command which I'm sure Smeagol would entirely appreciate) and deploy the resulting war file.

