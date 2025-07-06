/**
  @author: Dmytro Shkirmantsev <shkirmantsev@gmail.com>
*/
import Oauth2LoginLogoutManager from "./oidc-v1-m-Oauth2LoginLogoutManager.js";
import { OidcClientConfiguration } from "./oidc-v1-pkce-lib.js";
import PkceCodeChallengeVerifierStorage from "./oidc-v1-m-PkceCodeChallengeVerifierStorage.js";


class OidcTokenExchanger {

  static async exchangeCodeOnOidcTokens(authCode) {
    let codeVerifier = PkceCodeChallengeVerifierStorage.retrievePkceCodeVerifier();

    if (!codeVerifier) {
      try {
        await Oauth2LoginLogoutManager.forceLogin();
      } catch (error) {
        throw new Error(error);
      }
    }

    const tokenResponse = await OidcTokenExchanger.exchangeAuthCodeOnTokens(authCode, codeVerifier);

    const accessTokenValue = tokenResponse.access_token;
    const refreshTokenValue = tokenResponse.refresh_token;
    const idTokenValue = tokenResponse.id_token;
    const sessionStateValue = tokenResponse.session_state;
    const scopes = tokenResponse.scope;

    let oidcTokens = {
      access_token: accessTokenValue,
      refresh_token: refreshTokenValue,
      id_token: idTokenValue,
      session_state: sessionStateValue,
      scope: scopes,
    };

    OidcTokenExchanger.cleanHistoryForCodeExchange();

    return oidcTokens;
  }

  static cleanHistoryForCodeExchange() {
    window.history.replaceState(null, null, window.location.pathname);
  }

  static async exchangeAuthCodeOnTokens(authCode, codeVerifier) {
    const { tokenEndpoint, clientId, redirectUri, scope } = await OidcClientConfiguration.get();

    const tokenRequest = new Request(tokenEndpoint, {
      method: "POST",
      body: new URLSearchParams({
        grant_type: "authorization_code",
        code: authCode,
        redirect_uri: redirectUri,
        client_id: clientId,
        code_verifier: codeVerifier,
        scope: scope,
      }),
    });

    return fetch(tokenRequest)
      .then((response) => {
        if (!response.ok) {
          throw new Error(
            `Failed to exchange code for token (${response.status} ${response.statusText})`
          );
        }
        return response.json()
      });
  }

  /** 
   * @param {string} refreshToken 
   * @returns {Promise<Object>}  Object like this:
   * {
        "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6IC...",
        "token_type": "Bearer",
        "expires_in": 300,
        "refresh_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6IC...",
        "scope": "email profile"
      }
   *  @throws {Error} if the refresh token is invalid or expired.
      {
        "error": "invalid_grant",
        "error_description": "Refresh token is expired"
      }
   */
  static async requestRefreshedOidcTokens(refreshToken) {
    const { tokenEndpoint, clientId } = await OidcClientConfiguration.get();

    const requestOptions = {
      method: "POST",
      headers: {
        "Content-Type": "application/x-www-form-urlencoded",
      },
      body: new URLSearchParams({
        grant_type: "refresh_token",
        client_id: clientId,
        refresh_token: refreshToken,
      }),
    };

    try {
      const response = await fetch(tokenEndpoint, requestOptions);

      const data = await response.json();

      if (response.ok) {
        console.log("Access token:", data.access_token);
        console.log("Refresh token:", data.refresh_token);
        return data;
      } else {
        console.error("Error refreshing token:", data);
        throw new Error(data.error);
      }
    } catch (error) {
      console.error("Failed to refresh token:", error);
      throw error;
    }
  };
}

export default OidcTokenExchanger;
