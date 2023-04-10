/**
  @author: Dmytro Shkirmantsev <shkirmantsev@gmail.com>
*/
class AuthorizationUriBuilder {

    static buildUriFor(
        { authorizationEndpoint, clientId, redirectUri, scope, responseType },
        state,
        codeChallenge
    ) {
        const authorizationUrl = new URL(authorizationEndpoint);
        const searchParams = authorizationUrl.searchParams;

        searchParams.set("client_id", clientId);
        searchParams.set("redirect_uri", redirectUri);
        searchParams.set("scope", scope);
        searchParams.set("response_type", responseType);
        searchParams.set("state", state);
        searchParams.set("code_challenge", codeChallenge);
        searchParams.set("code_challenge_method", "S256");

        return authorizationUrl;
    };
};

export default AuthorizationUriBuilder;