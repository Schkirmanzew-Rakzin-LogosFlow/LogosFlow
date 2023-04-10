/**
  @author: Dmytro Shkirmantsev <shkirmantsev@gmail.com>
*/
class PkceCodeChallengeVerifierGenerator {

  static generateCodeVerifier = () => {
    return PkceCodeChallengeVerifierGenerator.uint8ArrayToBase64UrlEncodedString(crypto.getRandomValues(new Uint8Array(32)));
  };

  static async generateCodeChallengeValue(codeVerifier) {
    const encodedVerifier = await PkceCodeChallengeVerifierGenerator.sha256(codeVerifier);
    return PkceCodeChallengeVerifierGenerator.arrayBufferToBase64UrlEncodedString(encodedVerifier);
  }

  static uint8ArrayToBase64UrlEncodedString(sourceValueAsArray) {
    const encodedString = PkceCodeChallengeVerifierGenerator.encodeString(sourceValueAsArray);
    const trimmedString = PkceCodeChallengeVerifierGenerator.trimString(encodedString);
    const urlEncodedString = PkceCodeChallengeVerifierGenerator.replaceUnsafeChars(trimmedString);

    return urlEncodedString;
  }

  static encodeString(sourceValueAsArray) {
    const decodedString = String.fromCharCode(...sourceValueAsArray);
    const base64UrlEncodedString = btoa(decodedString);

    return base64UrlEncodedString;
  }

  static trimString(encodedString) {
    const trimmedString = encodedString.replace(/=*$/g, "");

    return trimmedString;
  }

  static replaceUnsafeChars(trimmedString) {
    const urlEncodedString = trimmedString.replace(/\+/g, "-").replace(/\//g, "_");

    return urlEncodedString;
  }

  static async sha256(plain) {
    const encodedValue = new Uint8Array([...plain].map((char) => char.charCodeAt(0)));

    return await crypto.subtle.digest("SHA-256", encodedValue);
  };

  // from https://stackoverflow.com/a/11562550/9014097
  static arrayBufferToBase64UrlEncodedString(buffer) {
    return PkceCodeChallengeVerifierGenerator.uint8ArrayToBase64UrlEncodedString(new Uint8Array(buffer));
  }
};

export default PkceCodeChallengeVerifierGenerator;
