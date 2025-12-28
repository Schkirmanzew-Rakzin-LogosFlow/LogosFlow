/**
  @author: Dmytro Shkirmantsev <shkirmantsev@gmail.com>
*/
class PkceCodeChallengeVerifierStorage {

  static CODE_CHALLENGE_KEY = "pkceCodeChallenge";
  static CODE_VERIFIER_KEY = "pkceCodeVerifier";

  static storeOauth2PkceCodeChallenge(codeChallenge) {
    window.localStorage.setItem(PkceCodeChallengeVerifierStorage.CODE_CHALLENGE_KEY, codeChallenge);

    return codeChallenge;
  }

  static storePkceCodeVerifier(codeVerifier) {
    window.localStorage.setItem(PkceCodeChallengeVerifierStorage.CODE_VERIFIER_KEY, codeVerifier);
    return codeVerifier;
  }

  static retrievePkceCodeVerifier() {
    return window.localStorage.getItem(PkceCodeChallengeVerifierStorage.CODE_VERIFIER_KEY);
  }

  static retrievePkceCodeChallenge() {
    return window.localStorage.getItem(PkceCodeChallengeVerifierStorage.CODE_CHALLENGE_KEY);
  }

  static clearPair() {
    window.localStorage.removeItem(PkceCodeChallengeVerifierStorage.CODE_VERIFIER_KEY);
    window.localStorage.removeItem(PkceCodeChallengeVerifierStorage.CODE_CHALLENGE_KEY);
  }
};

export default PkceCodeChallengeVerifierStorage;

