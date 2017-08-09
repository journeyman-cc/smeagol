## If you're using a small device
If you're using a small device, like a mobile phone, the top menu isn't usually displayed. Instead there will be an image like this:
![Menu icon](/img/three-lines.png)

at the top left of the page. Touching this image will cause the top menu to be displayed, and it will have all the options described in this documentation.

## Logging in
If you are not logged in, there will be an option `Log in` on the top menu. Note that if you're not an English language speaker, the menu items may be in your own language (provided we have a suitable language file). You must log in to edit pages, to change your password, or to perform administration.

Selecting the 'Log in' option will take you to the log in page. This will prompt you for your username and password. If you have just set up a new instance of Smeagol, your username will be `admin` and your password will be `admin`. **Note** It is very important to change this default password after logging in.

    +-------------------------------------------------------------------------+
    |                                   +----------------------------------+  |
    |  Your username:                   |                                  |  |
    |                                   +----------------------------------+  |
    |  Your password:                   |                                  |  |
    |                                   +----------------------------------+  |
    |                                   +-----------+                         |
    |  To edit this wiki                |  Log in!  |                         |
    |                                   +-----------+                         |
    +-------------------------------------------------------------------------+

Once you have entered your username and password, select the green `Log in!` button, or press `return` on your keyboard.

### Changing your password
To change your password, select the `Change password` option from the top menu. This will take you to the `Change password for...` page. This will prompt you for your (old) password, and your new password, twice. Complete the form and select the green `Change password!` button, or hit return.

    +-------------------------------------------------------------------------+
    |                                   +----------------------------------+  |
    |  Your password:                   |                                  |  |
    |                                   +----------------------------------+  |
    |  New password:                    |                                  |  |
    |                                   +----------------------------------+  |
    |  And again:                       |                                  |  |
    |                                   +----------------------------------+  |
    |                                   +--------------------+                |
    |  To change your password          |  Change password!  |                |
    |                                   +--------------------+                |
    +-------------------------------------------------------------------------+

If there is a problem (for example, the password wasn't long enough, or your new passwords didn't match), a message will show in red below the header of the page to explain the problem. Complete the form again.

If the form was submitted successfully and your password is changed, a message will be shown in green below the header of the page confirming this.

## Viewing page history
You can view the edit history of any page in the Wiki. At the top right of the content area, you will find a link `History`. Selecting this will take you to a page listing all the edits that have been made. Each row shows you:

* When the change was made
* What was changed (and who changed it)

It also provides a link `Show Version`, and a link `What's changed since?`

### Viewing a specific version
Selecting the `Show Version` link from any version in the page history will open the version of the page exactly as it was immediately after that edit. Selecting the `What's changed since?` link will show a page which highlights all the changes made to the page since that edit, with added text shown in green and deleted text in red.

## Editing pages
If you are logged in you can edit any page in the Wiki. At the top right of the content area, you will find a link `Edit this page`. Selecting this opens the page in the editor.

The editor is not strictly 'what you see is what you get', but it's fairly close to it. Icons on the ribbon above the content area allow you to apply simple styling. There are some prompts in the sidebar which will help with more complicate things.

### Markup syntax
Smeagol uses the Markdown format as provided by [markdown-clj](https://github.com/yogthos/markdown-clj), with the addition that anything enclosed in double square brackets, \[\[like this\]\], will be treated as a link into the wiki itself. Smeagol also supports [[Extensible Markup]].

## Uploading files
To upload a file (including an image file), select the link `Upload a file` from the top menu. **Warning:** do not do this while you are editing a page, or you will lose your edit!

Selecting the link will take you to the `Upload a file` page. This will prompt you for the file you wish to upload. Select your file, and then select the green `Save!` button.

After your file has uploaded, you will be shown a link which can be copied and pasted into a Wiki page to link to that file.

You must be logged in to upload files.

## Administering users

If you are an administrator, you can administer the users who are entitled to edit and administer the Wiki. When you are logged in, there will be a a link `Edit users` on the top menu. Selecting this will take you to the `Edit users` page which lists users, with an `Edit...` and a `Delete...` link for each. Below the existing users will be a link `Add new user`.

### Editing a user

Selecting the `Edit...` link for any user from the `Edit users` page, or the `Add new user` link from the same page, will take you to the `Edit user` form.

This page has the following inputs:

    +-------------------------------------------------------------------------+
    |                                   +----------------------------------+  |
    |  Username:                        |                                  |  |
    |                                   +----------------------------------+  |
    |  New password:                    |                                  |  |
    |                                   +----------------------------------+  |
    |  And again:                       |                                  |  |
    |                                   +----------------------------------+  |
    |  Email address:                   |                                  |  |
    |                                   +----------------------------------+  |
    |  Is Administrator?                [ ]                                   |
    |                                   +---------+                           |
    |  When you have finished editing   |  Save!  |                           |
    |                                   +---------+                           |
    +-------------------------------------------------------------------------+

#### To add a new user

When using this form after selecting the `Add new user` link, all fields will be blank. Complete at least the fields `Username`, `New password`, `And again`, and `Email address`. If the new user is to be an administrator, check the box labelled `Is Administrator`. Finally, select the green button marked `Save!`. Your new user will be saved.

#### To edit an existing user

When using this form after selecting the `Edit...` link against a particular user, the `Username` field will already be filled in (and you won't be able to edit it). The `Email address` field will also probably be filled in. If the user is an administrator, the `Is Administrator` box will be checked. The `New password` and `And again` fields will be blank. You may alter the email address or change the `Is Administrator` status.

#### To change the password of an existing user

Smeagol does not have a mechanism to allow users to reset their own password if they have forgotten it. Instead, they will have to ask an administrator to do this.

On the `Edit user` page for the existing user, enter their new password in the fields `New password` and `And again`; then select the green button marked `Save!`. **Warning** If you do not want to change the password, leave these fields blank!

## To log out

When you've finished editing the Wiki, you should log out. Select the `Log out` link from the top menu. This will take you to a very simple form:

    +-------------------------------------------------------------------------+
    |                                   +------------+                        |
    |  When you have finished editing   |  Log out!  |                        |
    |                                   +------------+                        |
    +-------------------------------------------------------------------------+

Select the red `Log out!` button to log out.

