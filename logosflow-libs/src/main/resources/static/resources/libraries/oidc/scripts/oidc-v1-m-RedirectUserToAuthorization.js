class RedirectUserToAuthorization {

    static redirectToUrl(authorizationUrl) {
        window.open(
            authorizationUrl,
            "authorizationRequestWindow",
            "width=800, height=600,left=200,top=200"
        );
    }
}

export default RedirectUserToAuthorization;