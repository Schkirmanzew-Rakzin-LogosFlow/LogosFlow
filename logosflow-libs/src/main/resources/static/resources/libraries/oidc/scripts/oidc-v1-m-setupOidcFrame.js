import { OidcClientConfiguration } from "./oidc-v1-pkce-lib.js";

export async function setupOIDCIFrame() {
    const oidcConfig = await OidcClientConfiguration.get();

    return new Promise((resolve) => {

        const iframe = document.createElement('iframe');
        iframe.id = 'oidc-session-iframe';
        iframe.src = oidcConfig.check_session_iframe;
        iframe.style.display = 'none';

        iframe.addEventListener('load', () => {
            resolve(iframe);
          });

        document.body.appendChild(iframe);
    });
}
