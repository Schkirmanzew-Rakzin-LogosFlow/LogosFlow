/**
  @author: Dmytro Shkirmantsev <shkirmantsev@gmail.com>
*/
import AuthorizationUriBuilder from "./oidc-v1-m-AuthorizationUriBuilder.js";
import AccesTokenManager from "./oidc-v1-m-AccesTokenManager.js";
import OidcTokensStorage from "./oidc-v1-m-OidcTokensStorage.js";
import { OidcClientConfiguration } from "./oidc-v1-pkce-lib.js";
import PageStateStorage from "./oidc-v1-m-PageStateStorage.js";
import PkceCodeChallengeVerifierGenerator from "./oidc-v1-m-PkceCodeChallengeVerifierGenerator.js";
import PageStateGenerator from "./oidc-v1-m-PageStateGenerator.js";
import PkceCodeChallengeVerifierStorage from "./oidc-v1-m-PkceCodeChallengeVerifierStorage.js";
import RedirectUserToAuthorization from "./oidc-v1-m-RedirectUserToAuthorization.js";


export class Oauth2LoginLogoutManager {

  static LOGIN_TIMEOUT_MS = 100;

  static async login() {
    try {
      if (await AccesTokenManager.isAuthenticationSuccessful()) {
        return;
      }

      await Oauth2LoginLogoutManager.forceLogin();
    } catch (error) {
      console.error(error);
      await Oauth2LoginLogoutManager.logout();
    }
  }

  static async forceLogin() {
    OidcTokensStorage.clear();

    let state = PageStateGenerator.generateState();
    state = PageStateStorage.storeOauth2State(state);

    let codeVerifier = PkceCodeChallengeVerifierGenerator.generateCodeVerifier();
    codeVerifier = PkceCodeChallengeVerifierStorage.storePkceCodeVerifier(codeVerifier);

    let codeChallenge = await PkceCodeChallengeVerifierGenerator.generateCodeChallengeValue(codeVerifier);
    codeChallenge = PkceCodeChallengeVerifierStorage.storeOauth2PkceCodeChallenge(codeChallenge);

    let authorizationUrlWithPromptUi = AuthorizationUriBuilder.buildUriFor(
      await OidcClientConfiguration.get(),
      state,
      codeChallenge
    );

    RedirectUserToAuthorization.redirectToUrl(authorizationUrlWithPromptUi);

    await Oauth2LoginLogoutManager.waitUntilLoginIsSuccessful();
  }

  static async waitUntilLoginIsSuccessful() {
    let isAuthenticationSuccessful = await AccesTokenManager.isAuthenticationSuccessful();
    if (isAuthenticationSuccessful) {
      return;
    }
    await new Promise(resolve => setTimeout(resolve, Oauth2LoginLogoutManager.LOGIN_TIMEOUT_MS));
    await Oauth2LoginLogoutManager.waitUntilLoginIsSuccessful();
  }

  static async logout() {
    const config = await OidcClientConfiguration.get();
    const idToken = OidcTokensStorage.getIdToken();
    Oauth2LoginLogoutManager.clearOauth2StoredData();

    if (!idToken) {
      console.error('No id_token found in local storage. Please authenticate first.');
      return;
    }

    const endSessionUrl = new URL(config.endSessionEndpoint);
    endSessionUrl.searchParams.append('id_token_hint', idToken);
    endSessionUrl.searchParams.append('post_logout_redirect_uri', window.location.origin);

    Oauth2LoginLogoutManager.redirectTo(endSessionUrl.href);
  }

  static clearOauth2StoredData() {
    PageStateStorage.clear();
    PkceCodeChallengeVerifierStorage.clearPair();
    OidcTokensStorage.clear();
  }

  static redirectTo(reference) {
    window.location.href = reference;
  }
};

export default Oauth2LoginLogoutManager;
