/**
  @author: Dmytro Shkirmantsev <shkirmantsev@gmail.com>
*/
import OidcTokenExchanger from "./oidc-v1-m-OidcTokenExchanger.js";
import PageStateStorage from "./oidc-v1-m-PageStateStorage.js";
import OidcTokensStorage from "./oidc-v1-m-OidcTokensStorage.js";
import { OidcClientConfiguration } from "./oidc-v1-pkce-lib.js";
import Oauth2LoginLogoutManager from "./oidc-v1-m-Oauth2LoginLogoutManager.js";


class AccesTokenManager {

  /**
   * 
   * @param {String | Object} scopes - in format "openid email profile" or ["openid", "email", "profile"]
   * @returns {Promise<String>} - OIDC JWT access token
   */
  static async getAccessToken(scopes = []) {
    let accessToken = OidcTokensStorage.getAccessToken();

    if (AccesTokenManager.isAccessTokenValid(accessToken, scopes)) {
      return accessToken;
    }

    return await AccesTokenManager.refreshAndGetAccessToken(scopes);
  }

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

  static async refreshAndGetAccessToken(scopes=[]) {
    const refreshToken = OidcTokensStorage.getRefreshToken();

    if (!refreshToken || OidcTokensStorage.isRefreshTokenExpired()) {
      await Oauth2LoginLogoutManager.forceLogin(scopes);
      if (await AccesTokenManager.isAuthenticationSuccessful()) {
        return await AccesTokenManager.getAccessToken();
      }

    }

    const oidcTokens = await OidcTokenExchanger.requestRefreshedOidcTokens(refreshToken);
    
    if(OidcTokensStorage.getSessionState()!=oidcTokens.oidcTokens["session_state"]){
      await Oauth2LoginLogoutManager.logout();
    }

    OidcTokensStorage.storeOidcTokens(oidcTokens);

    return OidcTokensStorage.getAccessToken();
  }
}

export default AccesTokenManager;