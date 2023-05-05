/**
  @author: Dmytro Shkirmantsev <shkirmantsev@gmail.com>
*/
class AuthorizationUriBuilder {

    static buildUriFor(
        { authorizationEndpoint, clientId, redirectUri, scope, responseType },
        state,
        codeChallenge,
        scopes = []
    ) {
        let scopesList;
        if (typeof scopes == 'string') {
            scopesList = scopes.split(' ');
        } else if (Array.isArray(scopes)) {
            scopesList = scopes;
        }

        scopesList = AuthorizationUriBuilder.mergeScopes(scopesList, scope);

        const authorizationUrl = new URL(authorizationEndpoint);
        const searchParams = authorizationUrl.searchParams;

        searchParams.set("client_id", clientId);
        searchParams.set("redirect_uri", redirectUri);
        searchParams.set("scope", scopesList.join(" "));
        searchParams.set("response_type", responseType);
        searchParams.set("state", state);
        searchParams.set("code_challenge", codeChallenge);
        searchParams.set("code_challenge_method", "S256");

        return authorizationUrl;
    };

    static mergeScopes(scopes, scope) {
        return scopes.concat(scope.split(" "));
    }
};

export default AuthorizationUriBuilder;