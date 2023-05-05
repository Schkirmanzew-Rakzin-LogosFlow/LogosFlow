/**
  @author: Dmytro Shkirmantsev <shkirmantsev@gmail.com>
*/
import AuthorizationUriBuilder from "./oidc-v1-m-AuthorizationUriBuilder.js";
import AccesTokenManager from "./oidc-v1-m-AccesTokenManager.js";
import OidcTokensStorage from "./oidc-v1-m-OidcTokensStorage.js";
import { setupOIDCIFrame } from "./oidc-v1-m-setupOidcFrame.js";
import { OidcClientConfiguration } from "./oidc-v1-pkce-lib.js";
import PageStateStorage from "./oidc-v1-m-PageStateStorage.js";
import PkceCodeChallengeVerifierGenerator from "./oidc-v1-m-PkceCodeChallengeVerifierGenerator.js";
import PageStateGenerator from "./oidc-v1-m-PageStateGenerator.js";
import PkceCodeChallengeVerifierStorage from "./oidc-v1-m-PkceCodeChallengeVerifierStorage.js";
import RedirectUserToAuthorization from "./oidc-v1-m-RedirectUserToAuthorization.js";


export class Oauth2LoginLogoutManager {

  static LOGIN_TIMEOUT_MS = 100;
  static CHECK_SESSION_TIMEOUT_MS = 5000;
  static OIDC_SESSION_IFRAME_ID = "oidc-session-iframe";

  static async login() {
    try {
      if (await AccesTokenManager.isAuthenticationSuccessful()) {
        return;
      }

      await Oauth2LoginLogoutManager.forceLogin();
      await (async function () {
        await setupOIDCIFrame();
        await Oauth2LoginLogoutManager.listenRemoteLogout()
      })();
    } catch (error) {
      console.error(error);
      await Oauth2LoginLogoutManager.logout();
    }
  }

  static async forceLogin(scopes=[]) {
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
      codeChallenge,
      scopes
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

  static async listenRemoteLogout() {
    const iframe = document.getElementById(Oauth2LoginLogoutManager.OIDC_SESSION_IFRAME_ID);
    let checkInterval;
    window.checkInterval = checkInterval;

    const config = await OidcClientConfiguration.get();
    const clientId = config.clientId;
    const sessionState = OidcTokensStorage.getSessionState();

    const iframeOrigin = new URL(iframe.src).origin;
    const message = `${clientId} ${sessionState}`;

    window.addEventListener('message', async (event) => {
      if (event.origin !== iframeOrigin) {
        return;
      }

      if (event.source === iframe.contentWindow) {
        if (event.data === 'changed') {

          clearInterval(checkInterval);
          await Oauth2LoginLogoutManager.logout();

        } else {
          /* */
        }
      }
    });

    setTimer();

    function setTimer() {
      check_session();
      checkInterval = setInterval(check_session, Oauth2LoginLogoutManager.CHECK_SESSION_TIMEOUT_MS);
    };

    function check_session() {
      let win = window.parent.frames[Oauth2LoginLogoutManager.OIDC_SESSION_IFRAME_ID].contentWindow;
      win.postMessage(message, iframeOrigin);
    }

  }
};

export default Oauth2LoginLogoutManager;
