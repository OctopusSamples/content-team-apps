package com.octopus.octopusoauth.application.http;

import com.octopus.octopusoauth.domain.handlers.OctopusOauthHandler;
import com.octopus.octopusoauth.domain.handlers.SimpleResponse;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.CookieParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

/**
 * When this app is run as a web server, this class defines the REST API endpoints.
 */
@Path("/oauth/octopus")
@RequestScoped
public class OctopusOauthResource {

  @Inject
  OctopusOauthHandler octopusOauthHandler;

  /**
   * The oauth redirection endpoint.
   *
   * @param body The request body.
   * @return a HTTP response object.
   */
  @POST
  public Response redirect(final String body, @CookieParam("appBuilderOctopusNonce") final String nonce) {
    final SimpleResponse simpleResponse = octopusOauthHandler.redirectToClient(body, nonce);
    final ResponseBuilder response = Response.status(simpleResponse.getCode(),
        simpleResponse.getBody());
    simpleResponse.getHeaders().forEach(response::header);
    return response.build();
  }
}
