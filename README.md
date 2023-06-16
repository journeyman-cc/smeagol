![One wiki to rule them all](https://www.weft.scot/images/smeagol.png)

# Welcome to Smeagol!
Smeagol is a hackable, extensible Wiki engine which is reasonably user-friendly. It uses [Markdown](http://daringfireball.net/projects/markdown/) as its text format, and [Git](http://git-scm.com/) to provide versioning and backup.

**NOTE** Smeagol is no longer actively maintained; I have moved on to using [Cryogen](http://cryogenweb.org/) as my preferred system for generating websites. There's a lot of good ideas in Smeagol, but doing markdown to HTML conversion every time a document is requested puts load on the server which isn't really merited. If anyone else would like to take on the project, they would be welcome to do so.

## Using Smeagol
Read the [[User Documentation]] for an introduction to all Smeagol's features.

## Markup syntax
Smeagol uses the Markdown format as provided by [markdown-clj](https://github.com/yogthos/markdown-clj), with the addition that anything enclosed in double square brackets, \[\[like this\]\], will be treated as a link into the wiki itself. The markdown format is extensible, and has extensions already for inclusions, for data visualisations and for picture galleries. Read more about [[Extensible Markup]].

## Security and authentication
Smeagol now has good security and authentication. While the initial password supplied with the system is not encrypted, when it is changed it will be; and passwords for new users added through the user administration pages are encrypted. Read more about [[Security and authentication]].

## Internationalisation
Smeagol has built in internationalisation. Currently it has translation files for English, German, Lithuanian and Russian. We'd welcome volunteers to translate it into other languages.

## Images
You can (if you're logged in) upload files, including images, using the **Upload a file** link on the top menu bar. You can link to an uploaded image, or to other images already available on the web, like this:

![Smeagol](http://vignette3.wikia.nocookie.net/lotr/images/e/e1/Gollum_Render.png/revision/latest?cb=20141218075509)

## Running Smeagol
You can run Smeagol from the [[Docker Image]]; alternatively you can run it from an executable jar file or as a war file in a servlet container. Read how about [[Configuring Smeagol]] and [[Deploying Smeagol]].

## Developing Smeagol
Smeagol is an open source project; you're entitled to make changes yourself. Read more about  [[Developing Smeagol]].

## License
Copyright © 2014-2020 Simon Brooke. Licensed under the GNU General Public License,
version 2.0 or (at your option) any later version. If you wish to incorporate
parts of Smeagol into another open source project which uses a less restrictive
license, please contact me; I'm open to dual licensing it.

## Phoning home
Smeagol does currently fetch one image from my home site. Read more about [[Phoning Home]], and how to prevent it (if you want to).

## Advertisement
If you like what you see here, I am available for work on open source Clojure projects.
