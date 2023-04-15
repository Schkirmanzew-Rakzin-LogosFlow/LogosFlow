/**
  @author: Dmytro Shkirmantsev <shkirmantsev@gmail.com>
*/
import Oauth2LoginLogoutManager from "./oidc-v1-m-Oauth2LoginLogoutManager.js";
import OnOAuth2AuthorizedHandler from "./oidc-v1-m-onOauth2AuthorizeHandler.js";
import AccesTokenManager from "./oidc-v1-m-AccesTokenManager.js";

const OIDC_SERVER_WELL_KNOWN_CONFIGURATION_URL = "http://localhost:49088/realms/logosflow/.well-known/openid-configuration";

window.OIDC_SERVER_WELL_KNOWN_CONFIGURATION_URL = OIDC_SERVER_WELL_KNOWN_CONFIGURATION_URL;
window.OIDC_CLIENT_ID = "logosflow-frontend-public";

export class OidcClientConfiguration {
  static config = null;

  static async get() {
    if (OidcClientConfiguration.config) {
      return OidcClientConfiguration.config;
    }

    const response = await fetch(OIDC_SERVER_WELL_KNOWN_CONFIGURATION_URL);
    const data = await response.json();

    OidcClientConfiguration.config = {
      authorizationEndpoint: data.authorization_endpoint,
      tokenEndpoint: data.token_endpoint,
      userInfoEndpoint: data.userinfo_endpoint,
      endSessionEndpoint: data.end_session_endpoint,
      clientId: window.OIDC_CLIENT_ID,
      redirectUri: window.location.origin + "/authcodeReader.html",
      scope: "openid email profile",
      responseType: "code",
      pkce: true,
    };

    return OidcClientConfiguration.config;
  }
}

export const OidcPkceUtils = {
  oidcClientConfiguration: OidcClientConfiguration,
  /**
  * asynchoronous function. Need always to await it.!!!!
  */
  oAuth2login: Oauth2LoginLogoutManager.login,
  oAuth2logout: Oauth2LoginLogoutManager.logout,
  /**
   * asynchoronous function. Need always to await it.!!!!
   */
  forceLogin: Oauth2LoginLogoutManager.forceLogin,
  onOauth2Authorize: OnOAuth2AuthorizedHandler.handle,
  getAccessToken: AccesTokenManager.getAccessToken
};

window.OidcPkceUtils = OidcPkceUtils;

export default OidcPkceUtils;
