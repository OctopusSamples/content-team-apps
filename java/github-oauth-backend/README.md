This project creates Lambda that implement the GitHub OAuth flow documented [here](https://docs.github.com/en/developers/apps/building-oauth-apps/authorizing-oauth-apps).

GitHub applications do not support the Implicit Flow. This means we must handle the OAuth flow with a server side service, where the client
secret can be kept safe.

Because the applications are Lambdas, there is no session cache in which to save things like access tokens. The solution is to send the access token back to the client
in an encrypted "session" cookie, inspired by the form-based authentication documented [here](https://quarkus.io/guides/security-built-in-authentication#form-auth).

So the lambdas implemented in this proxy do the following:
* Create a random state code, and save it as a cookie (see the Quarkus [CodeAuthenticationMechanism](https://github.com/quarkusio/quarkus/blob/main/extensions/oidc/runtime/src/main/java/io/quarkus/oidc/runtime/CodeAuthenticationMechanism.java#L253) class for an example of this).
* Redirect the user to log in via GitHub.
* Handle the redirect from GitHub, check the state, and exchange the code for an access token.
* Encrypt the token and send it back in a cookie with a redirect to the SPA. 
