/**
  @author: Dmytro Shkirmantsev <shkirmantsev@gmail.com>
*/
class OidcAuthCodeReader {

    static async consumeRedirectedOauthCodeResponse() {
        let urlParams = new URLSearchParams(window.location.search);
        let authCode = urlParams.get('code'),
            state = urlParams.get('state'),
            error = urlParams.get('error'),
            errorDescription = urlParams.get('error_description');

        alertIfError(error, errorDescription);
        await postAuthorizeInParentIfSuccess(authCode, state, error);


        function alertIfError(error, errorDescription) {
            if (error) {
                window.alert("The following error occurred during authentication:\n " + error + "\n Description: \n" + errorDescription);
                window.close();
            }
        };

        async function postAuthorizeInParentIfSuccess(code, state, error) {
            if (error) {
                window.opener.OidcPkceUtils.oAuth2logout();
                window.close();
                return;
            }

            await window.opener.OidcPkceUtils.onOauth2Authorize(state, code, window);
            window.close();
        };
    }
}

export default OidcAuthCodeReader;