You need a way to work on markdown documents from GitHub on your machine and then save them back to GitHub. This is the process I follow:
GitHub desktop lets you download repos from GitHub, work on them locally and then commit/save your changes to the repo. Download GitHub Desktop here: https://desktop.github.com/
Install it, and then sign in to the app on your computer with your GitHub username and password.
Next navigate to the repository: https://github.com/OctopusDeploy/octopus-messaging
Click the green button *Clone or download* and click *Open in Desktop*. This opens GitHub Desktop and asks where on your machine you’d like to store your local copy of the repo.
From there you can open the files in the repo locally (I use Atom www.atom.io to write markdown files) and make any changes.
After you’ve made edits, added new files, etc, if you open GitHub Desktop you’ll see the changes you’ve made, which you can commit (add to the repo everybody uses) by adding a summary of the changes, click *Commit to master*, and then click *Push to Origin* which saves your files to the GitHub repo on GitHub.com.
One tip to avoid conflicts between your edits and edits somebody else might have made. Before you edit your files, make sure you have the latest version of the files by opening GitHub Desktop and clicking *Fetch Origin*.