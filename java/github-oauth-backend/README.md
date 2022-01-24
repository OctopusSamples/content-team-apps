This project creates Lambda that implement the GitHub OAuth flow documented [here](https://docs.github.com/en/developers/apps/building-oauth-apps/authorizing-oauth-apps).

GitHub applications require special handling because they do not support OpenID, and do not support the client credential flow (2-legged OAuth). This means we are unable
to use the traditional Quarkus authentication framework, and must provide a server side service to handle the token exchanging.

Because the applications are Lambdas, there is no session cache in which to save things like access tokens. The solution is to send the access token back to the client
in an encrypted cookie, inspired by the form-based authentication documented [here](https://quarkus.io/guides/security-built-in-authentication#form-auth).

So the lambdas implemented in this proxy do the following:
* Create a random state code, and persist it to a database.
* Redirect the user to log in via GitHub.
* Handle the redirect from GitHub, check the state, and exchange the code for an access token.
* Encrypt the token and send it back in a cookie with a redirect to the SPA.