Security is now greatly improved. There is a file called *passwd* in the *resources* directory, which contains a clojure map which maps usernames to maps with plain-text passwords and emails thus:

    {:admin {:password "admin" :email "admin@localhost" :admin true}
     :adam {:password "secret" :email "adam@localhost"}}

that is to say, the username is a keyword and the corresponding password is a string. However, since version 0.5.0, users can now change their own passwords, and when the user changes their password their new password is encrypted using the [scrypt](http://www.tarsnap.com/scrypt.html) one-way encryption scheme. The password file is now no longer either in the *resources/public* directory so cannot be downloaded through the browser, nor in the git archive to which the Wiki content is stored, so that even if that git archive is remotely clonable an attacker cannot get the password file that way.
