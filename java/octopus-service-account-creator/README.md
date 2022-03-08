# Octopus Service Account Creator
This service uses an Octofront ID token to create a service account in an Octopus cloud instance.

The ID Token is created and encrypted by the `octopus-oauth-backend` service, saved by the client
as a cookie, and decrypted by this service to perform the Octopus login.

# Endpoints

* `POST` `/api/serviceaccounts` - Create a service account.