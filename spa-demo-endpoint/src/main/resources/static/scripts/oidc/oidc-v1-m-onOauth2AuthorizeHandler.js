/**
  @author: Dmytro Shkirmantsev <shkirmantsev@gmail.com>
*/
import PageStateStorage from "./oidc-v1-m-PageStateStorage.js";
import OidcTokensStorage from "./oidc-v1-m-OidcTokensStorage.js";
import Oauth2LoginLogoutManager from "./oidc-v1-m-Oauth2LoginLogoutManager.js";
import OidcTokenExchanger from "./oidc-v1-m-OidcTokenExchanger.js";


class OnOAuth2AuthorizedHandler {

  static async handle(state, authCode, dialogWindow) {

    const originalStateValue = PageStateStorage.retrieveOauth2State();

    if (!originalStateValue) {
      throw new Error("Failed to retrieve OAuth2State during browser authorization flow");
    }

    try {
      if (state === originalStateValue) {
        const oidcTokens = await OidcTokenExchanger.exchangeCodeOnOidcTokens(authCode);
        OidcTokensStorage.storeOidcTokens(oidcTokens);
        return;
      }
    } catch (error) {
      console.log("Error exchanging code for OIDC Tokens", error);
      await Oauth2LoginLogoutManager.logout();
    } finally {
      closeAuthDialogWindow(dialogWindow);
    }

    await Oauth2LoginLogoutManager.logout();

    function closeAuthDialogWindow(dialogWindow) {
      try {
        dialogWindow.close();
      } catch (error) { console.error(error); }
    }
  };
}

export default OnOAuth2AuthorizedHandler;
