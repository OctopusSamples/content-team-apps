# Dumb Reverse Proxy (DRP)

This project implements a simple reverse proxy Lambda capable of forwarding requests to a downstream HTTP service, Lambda,
or SQS queues.

The intention of this proxy is to allow Lambda requests to be routed to feature branch instances. The instances can be
hosted on a developers local machine and exposed to public traffic with services like [ngrok](https://ngrok.com/) or via
a more direct tunnel such as a VPN. Alternatively, feature branch Lambdas can be deployed alongside their mainline
siblings.

The functionality was inspired by the Uber post 
[Why We Leverage Multi-tenancy in Uber’s Microservice Architecture ](https://eng.uber.com/multitenancy-microservice-architecture/).

# History

API Gateway is a rich platform for exposing backend services like Lambdas, and offers a great deal of flexibility in
terms of validating and modifying requests before they are sent to a Lambda, and then modifying the response before
it is sent to the client.

API Gateway also has some limited functionality for selecting the backend Lambda with each request using 
[stage variables](https://aws.amazon.com/blogs/compute/using-api-gateway-stage-variables-to-manage-lambda-functions/).

Unfortunately, stage variables are static, meaning Lambdas can only be selected per stage rather than dynamically
selected per request. This limitation prevents anything more than simple routing rules.

What we required to support feature branch deployments of microservices was something like a reverse proxy
or service mesh routing each request. Platforms like Kubernetes have a wealth of options, while traditional VM based
application hosting can make use of tools like NGINX, but there were no good examples of this kind of functionality
that could be run as a Lambda.

The [HTTP reverse proxy built into Go](https://go.dev/src/net/http/httputil/reverseproxy.go), combined with the fact
that Go has a quick boot time, made it an ideal language to write a Lambda reverse proxy in. The AWS SDK
made it easy to pass requests to other services like Lambdas and SQS.

# Limitations

This project is not designed to be a generic reverse proxy. It supports simple routing rules through predetermined headers,
and has strong opinions about how HTTP requests are translated to SQS messages based [JSON:API](https://jsonapi.org/)
semantics.

But the code is easy enough to modify if anyone is looking for a place to start.

# How to perform redirections

Redirection rules are defined in the `Routing` header based on ant wildcard paths. For example, the header 
`route[/api/products*:GET]=url[https://c9ce-118-208-2-185.ngrok.io]` instructs this proxy to redirect all `GET` requests made on
paths that match `/api/products*` to https://c9ce-118-208-2-185.ngrok.io. A header like
`route[/api/products*:GET]=lambda[Development-products-0-myfeature]` will redirect `GET` requests made on
paths that match `/api/products*` to the Lambda called `Development-products-0-myfeature`.

This allows a client to make a request to a top level API with `Routing` headers like 
`route[/api/products*:GET]=https://c9ce-118-208-2-185.ngrok.io;route[/api/audits*:GET]=lambda[Development-audits-0-myfeature]`,
and so long as each service forwards the `Routing` header to each service it calls, feature branch instances of 
deeply nested microservices will be executed without having to recreate the entire microservice ecosystem locally.

# Redirection rules

* HTTP - `route[ant path:method]=url[http://urlgoeshere]`
* Lambda - `route[ant path:method]=lambda[lambda name or arn]`
* SQS - `route[ant path:method]=sqs[queue name]`
* Reference redirection on another path - `route[antpath:method]=path[ant path and method whose redirection rules will be used]`

# Security

This project is integrated with Cognito and requires users performing redirection to supply a valid access token
with the correct group membership in order for routing rules to be applied.

The `COGNITO_AUTHORIZATION_REQUIRED` environment variable can be set to `fasle` to disable any authentication rules.

# Examples

## Reuse one redirection rule

The `path` redirection performs a lookup of the redirection rule assigned to another path. This allows you to define
one redirection rule to a `url`, `sqs`, or `lambda`, and then reference it from multiple other redirection rules.

`route[/api/products*:GET]=https://c9ce-118-208-2-185.ngrok.io;route[/api/products/**/*]=path[/api/products*:GET]`
