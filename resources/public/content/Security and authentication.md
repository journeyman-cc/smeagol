Security is now greatly improved over earlier releases of Smeagol. There is a file called `passwd` which by default is in the `resources` directory, which contains a [`Clojure` map](https://clojure.org/reference/data_structures#Maps) which maps usernames to maps with plain-text passwords and emails thus:

    {:admin {:password "admin" :email "admin@localhost" :admin true}
     :adam {:password "secret" :email "adam@localhost"}}

that is to say, the username is a keyword and the corresponding password is a string. However, since version 0.5.0, users can now change their own passwords, and when the user changes their password their new password is encrypted using the [scrypt](http://www.tarsnap.com/scrypt.html) one-way encryption scheme. The password file is now no longer either in the `resources/public` directory so cannot be downloaded through the browser, nor in the git archive to which the Wiki content is stored, so that even if that git archive is remotely clonable an attacker cannot get the password file that way.

## Fields in the user record
Keys and their associated values in the individual user's record are as follows:

* `:password`  The user's password, which can be plain text (if set via the user interface, an encrypted password is stored)
* `:email`  The user's email address (not currently used; may be used in future for sending password reset messages)
* `:admin`  If present and set to `true`, the user has access to the user administration functions.

## Maintaining the passwd file outside the Smeagol deployment
You may set an environment variable, `SMEAGOL_PASSWD`, to indicate a `passwd` file anywhere you like on the file system provided the process running Smeagol can read it; but unless the file is writable by the process which Smeagol runs as you will not be able to administer users through the user interface.

Of course, it is possible to edit the file using a text editor and maintain the list of allowed users in that way.