package com.octopus.octopusoauth.application.http;

import com.octopus.octopusoauth.domain.handlers.OctopusOauthLoginHandler;
import com.octopus.octopusoauth.domain.handlers.OctopusOauthRedirectHandler;
import com.octopus.octopusoauth.domain.handlers.SimpleResponse;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import com.octopus.octopusoauth.OauthBackendConstants;

/**
 * When this app is run as a web server, this class defines the REST API endpoints.
 */
@Path("/oauth/octopus")
@RequestScoped
public class OctopusOauthResource {

  @Inject
  OctopusOauthRedirectHandler octopusOauthRedirectHandler;

  @Inject
  OctopusOauthLoginHandler octopusOauthLoginHandler;

  /**
   * The oauth redirection endpoint.
   *
   * @param body The request body.
   * @return a HTTP response object.
   */
  @Path("response")
  @POST
  public Response redirect(final String body, @CookieParam(OauthBackendConstants.STATE_COOKIE) final String nonce) {
    final SimpleResponse simpleResponse = octopusOauthRedirectHandler.redirectToClient(body, nonce);
    return buildResponse(simpleResponse);
  }

  @Path("login")
  @GET
  public Response login() {
    final SimpleResponse simpleResponse = octopusOauthLoginHandler.redirectToLogin();
    return buildResponse(simpleResponse);
  }

  private Response buildResponse(final SimpleResponse simpleResponse) {
    final ResponseBuilder response = Response.status(simpleResponse.getCode(),
        simpleResponse.getBody());
    simpleResponse.getHeaders().forEach(response::header);
    return response.build();
  }
}
