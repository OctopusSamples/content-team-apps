This project handles the login flow with Octofront, handling the login response, encrypting
the ID token, and passing it back to the client in a cookie. So the flow looks like:

1. Client opens Octofront login page.
2. Octofront POSTs an ID token back to this service.
3. This service encrypts it, and redirects the client back to the original app with the token in a cookie.