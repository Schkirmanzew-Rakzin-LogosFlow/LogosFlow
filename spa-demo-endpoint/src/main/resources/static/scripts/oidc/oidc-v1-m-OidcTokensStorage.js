/**
  @author: Dmytro Shkirmantsev <shkirmantsev@gmail.com>
*/
import TokenParser from "./oidc-v1-m-TokenParser.js";


class OidcTokensStorage {

  static ACCESS_TOKEN_KEY = "access_token";
  static REFRESH_TOKEN_KEY = "refresh_token";
  static ID_TOKEN_KEY = "id_token";
  static ACCESS_TOKEN_EXPIRATION_TIME_KEY = "access_token_exp_time";
  static REFRESH_TOKEN_EXPIRATION_TIME_KEY = "refresh_token_exp_time";
  static ID_TOKEN_EXPIRATION_TIME_KEY = "id_token_exp_time";
  static SCOPES = "scope";
  static SESSION_STATE_KEY = "session_state";
  static EXPIRATION_TIME_BUFFER_STOCK = 5 * 1000; // 5 seconds

  /** 
   * * @param {Object} oidcTokens - An object containing the OIDC tokens.
   * @returns {Object} Object like this:
   * {
        "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6IC...",
        "token_type": "Bearer",
        "expires_in": 300,
        "refresh_token": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6IC...",
        "scope": "email profile",
        "id_token": "your_id_token",
        "session_state": "your_session_state"
      }
   * @throws {Error} if the refresh token is invalid or expired.
      {
        "error": "invalid_grant",
        "error_description": "Refresh token is expired"
      }
   */
  static storeOidcTokens(oidcTokens) {
    window.localStorage.setItem(OidcTokensStorage.ACCESS_TOKEN_KEY, oidcTokens["access_token"]);
    window.localStorage.setItem(
      OidcTokensStorage.ACCESS_TOKEN_EXPIRATION_TIME_KEY,
      OidcTokensStorage.expirationTimeUtcOfAccessToken(oidcTokens)
    );
    window.localStorage.setItem(OidcTokensStorage.REFRESH_TOKEN_KEY, oidcTokens["refresh_token"]);
    window.localStorage.setItem(
      OidcTokensStorage.REFRESH_TOKEN_EXPIRATION_TIME_KEY,
      OidcTokensStorage.expirationTimeUtcOfRefreshToken(oidcTokens)
    );
    window.localStorage.setItem(OidcTokensStorage.ID_TOKEN_KEY, oidcTokens["id_token"]);
    window.localStorage.setItem(
      OidcTokensStorage.ID_TOKEN_EXPIRATION_TIME_KEY,
      OidcTokensStorage.expirationTimeUtcOfIdToken(oidcTokens)
    );
    window.localStorage.setItem(OidcTokensStorage.SCOPES, oidcTokens["scope"]);
    window.localStorage.setItem(OidcTokensStorage.SESSION_STATE_KEY, oidcTokens["session_state"]);

    return oidcTokens;
  }

  /** 
   * @returns {String}
   */
  static getAccessToken() {
    return window.localStorage.getItem(OidcTokensStorage.ACCESS_TOKEN_KEY);
  }

  /** 
   * @returns {Number} UTC +0 in milliseconds
   */
  static getAccessTokenExpirationTimeUtc() {
    return Number(window.localStorage.getItem(OidcTokensStorage.ACCESS_TOKEN_EXPIRATION_TIME_KEY));
  }

  /** 
   * @returns {String}
   */
  static getRefreshToken() {
    return window.localStorage.getItem(OidcTokensStorage.REFRESH_TOKEN_KEY);
  }

  /** 
   * @returns {Number} UTC +0 in milliseconds
   */
  static getRefreshTokenExpirationTimeUtc() {
    return Number(window.localStorage.getItem(OidcTokensStorage.REFRESH_TOKEN_EXPIRATION_TIME_KEY));
  }

  /** 
   * @returns {String}
   */
  static getIdToken() {
    return window.localStorage.getItem(OidcTokensStorage.ID_TOKEN_KEY);
  }

  /** 
   * @returns {Number} UTC +0 in milliseconds
   */
  static getIdTokenExpirationTimeUtc() {
    return Number(window.localStorage.getItem(OidcTokensStorage.ID_TOKEN_EXPIRATION_TIME_KEY));
  }

  /** 
   * @returns {String} like "prifile email openid"
   */
  static getScopes() {
    const scopes = window.localStorage.getItem(OidcTokensStorage.SCOPES);
    if (!scopes) {
      return "";
    };
    return scopes;
  }

  static getSessionState() {
    return window.localStorage.getItem(OidcTokensStorage.SESSION_STATE_KEY);
  }

  static clear() {
    window.localStorage.removeItem(OidcTokensStorage.ACCESS_TOKEN_KEY);
    window.localStorage.removeItem(OidcTokensStorage.ACCESS_TOKEN_EXPIRATION_TIME_KEY);
    window.localStorage.removeItem(OidcTokensStorage.REFRESH_TOKEN_KEY);
    window.localStorage.removeItem(OidcTokensStorage.REFRESH_TOKEN_EXPIRATION_TIME_KEY);
    window.localStorage.removeItem(OidcTokensStorage.ID_TOKEN_KEY);
    window.localStorage.removeItem(OidcTokensStorage.ID_TOKEN_EXPIRATION_TIME_KEY);
    window.localStorage.removeItem(OidcTokensStorage.SCOPES);
    window.localStorage.removeItem(OidcTokensStorage.SESSION_STATE_KEY);
  }

  /** 
   * @returns {Boolean}
   */
  static isAccessTokenExpired() {
    const tokenExpirationTime = window.localStorage.getItem(OidcTokensStorage.ACCESS_TOKEN_EXPIRATION_TIME_KEY);
    return OidcTokensStorage.isTimeExpired(tokenExpirationTime, OidcTokensStorage.EXPIRATION_TIME_BUFFER_STOCK);
  }

  /** 
   * @returns {Boolean}
   */
  static isRefreshTokenExpired() {
    const tokenExpirationTime = Number(window.localStorage.getItem(OidcTokensStorage.REFRESH_TOKEN_EXPIRATION_TIME_KEY));
    return OidcTokensStorage.isTimeExpired(tokenExpirationTime, OidcTokensStorage.EXPIRATION_TIME_BUFFER_STOCK);
  }

  /** 
   * @param {Number} refreshTokenExpirationTime - UTC +0 in milliseconds
   * @param {Number} buffer - expiration time buffer stock (for requesting) in milliseconds
   * @returns {Boolean}
   */
  static isTimeExpired(refreshTokenExpirationTime, buffer = 0) {
    if (!refreshTokenExpirationTime || refreshTokenExpirationTime === 0) {
      return true;
    }
    return refreshTokenExpirationTime + buffer < new Date().getTime();
  }

  /**
   * 
   * @param {Object} oidcTokens - Object representation of the JSON response from the token endpoint.
   * @returns {Number} UTC +0 in milliseconds
   */
  static expirationTimeUtcOfAccessToken(oidcTokens) {
    const accessToken = oidcTokens["access_token"];
    if (!accessToken) {
      return 0;
    }
    return TokenParser.getExpirationTimeUtc(accessToken);
  }

  /**
   * 
   * @param {Object} oidcTokens - Object representation of the JSON response from the token endpoint.
   * @returns {Number} UTC +0 in milliseconds
   */
  static expirationTimeUtcOfRefreshToken(oidcTokens) {
    const refreshToken = oidcTokens["refresh_token"];
    if (!refreshToken) {
      return 0;
    }
    return TokenParser.getExpirationTimeUtc(refreshToken);
  }

  /**
   * 
   * @param {Object} oidcTokens - Object representation of the JSON response from the token endpoint.
   * @returns {Number} UTC +0 in milliseconds
   */
  static expirationTimeUtcOfIdToken(oidcTokens) {
    const idToken = oidcTokens["id_token"];
    if (!idToken) {
      return 0;
    }
    return TokenParser.getExpirationTimeUtc(idToken);
  }
};

export default OidcTokensStorage;
