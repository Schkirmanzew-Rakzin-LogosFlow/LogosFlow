/**
  @author: Dmytro Shkirmantsev <shkirmantsev@gmail.com>
*/
import OidcTokensStorage from "./oidc-v1-m-OidcTokensStorage.js";
import PageStateStorage from "./oidc-v1-m-PageStateStorage.js";
import { OidcClientConfiguration } from "./oidc-v1-pkce-lib.js";



class AccesTokenManager {

  static async isAuthenticationSuccessful() {

    const state = PageStateStorage.retrieveOauth2State();
    if (!state) { return false; }

    let accessToken = OidcTokensStorage.getAccessToken();
    if (!accessToken) { return false; }

    let refreshToken = OidcTokensStorage.getRefreshToken();
    if (!refreshToken) { return false; }

    const oidcClientConfiguration = await OidcClientConfiguration.get();
    const userInfoEndpoint = oidcClientConfiguration.userInfoEndpoint;

    let userInfoUrl = new URL(userInfoEndpoint);

    return await fetch(userInfoUrl, {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Authorization': 'Bearer ' + accessToken
      }
    }
    ).then(response => response.ok).catch(error => false);
  }

  static isAccessTokenValid(accessToken, scopes = []) {
    if (!accessToken) {
      return false;
    }
    if (OidcTokensStorage.isAccessTokenExpired() || OidcTokensStorage.isRefreshTokenExpired()) {
      return false;
    };

    return AccesTokenManager.checkStoredTokenContains(scopes);
  }

  static checkStoredTokenContains(scopes) {
    let scopesList;
    if (typeof scopes == 'string') {
      scopesList = scopes.split(' ');
    };
    if (Array.isArray(scopes)) {
      scopesList = scopes;
    }
    if (!scopes || scopes.length === 0) {
      return true;
    }
    let storedTokenScopes = new Set(OidcTokensStorage.getScopes().split(' '));
    return scopesList.every(scope => storedTokenScopes.has(scope));
  }
}

export default AccesTokenManager;