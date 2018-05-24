![One wiki to rule them all](https://www.weft.scot/images/smeagol.png)

# Welcome to Smeagol!
Smeagol is a simple Wiki engine inspired by [Gollum](https://github.com/gollum/gollum/wiki). Gollum is a Wiki engine written in Ruby, which uses a number of simple text formats including [Markdown](http://daringfireball.net/projects/markdown/), and which uses [Git](http://git-scm.com/) to provide versioning and backup. I needed a new Wiki for a project and thought Gollum would be ideal - but unfortunately it doesn't provide user authentication, which I needed, and it was simpler for me to reimplement the bits I did need in Clojure than to modify Gollum.

So at this stage Smeagol is a Wiki engine written in Clojure which uses Markdown as its text format, which does have user authentication, and which uses Git as its versioning and backup system.

## Status
Smeagol is now a fully working small Wiki engine, and meets my own immediate needs.

## Using Smeagol
Read the [[User Documentation]] for an introduction to all Smeagol's features.

## Markup syntax
Smeagol uses the Markdown format as provided by [markdown-clj](https://github.com/yogthos/markdown-clj), with the addition that anything enclosed in double square brackets, \[\[like this\]\], will be treated as a link into the wiki itself. Read more about [[Extensible Markup]].

## Security and authentication
Smeagol now has good security and authentication. While the initial password supplied with the system is not encrypted, when it is changed it will be; and passwords for new users added through the user administration pages are encrypted. Read more about [[Security and authentication]].

## Internationalisation
Smeagol has built in internationalisation. Currently it has translation files for English, German, Lithuanian and Russian. We'd welcome volunteers to translate it into other languages.

## Images
You can (if you're logged in) upload files, including images, using the **Upload a file** link on the top menu bar. You can link to an uploaded image, or to other images already available on the web, like this:

![Smeagol](http://vignette3.wikia.nocookie.net/lotr/images/e/e1/Gollum_Render.png/revision/latest?cb=20141218075509)

## Running Smeagol
You can run Smeagol from the [[Docker Image]]; alternatively you can run it from an executable jar file or as a war file in a servlet container. Read how in [[Deploying Smeagol]].

## Developing Smeagol
Smeagol is an open source project; you're entitled to make changes yourself. Read more about [[Developing Smeagol]].

## License
Copyright © 2014-2017 Simon Brooke. Licensed under the GNU General Public License,
version 2.0 or (at your option) any later version. If you wish to incorporate
parts of Smeagol into another open source project which uses a less restrictive
license, please contact me; I'm open to dual licensing it.

## Phoning home
Smeagol does currently fetch one image from my home site. Read more about [[Phoning Home]], and how to prevent it (if you want to).

## Advertisement
If you like what you see here, I am available for work on open source Clojure projects.
