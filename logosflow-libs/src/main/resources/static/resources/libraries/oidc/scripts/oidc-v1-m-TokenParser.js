/**
  @author: Dmytro Shkirmantsev <shkirmantsev@gmail.com>
*/
class TokenParser {

    /** 
     * @param {String} jwtTokenAsJwtString - the JWT string
     * @returns {String | null} the JWT payload as a JSON string
     */
    static parseJwtToJsonPayloadAsString(jwtTokenAsJwtString) {
        try {
            const base64Url = jwtTokenAsJwtString.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            return new TextDecoder('utf-8').decode(base64ToByteArray(base64));
        } catch (error) {
            console.error('Error while parsing JWT:', error);
            return null;
        }

        function base64ToByteArray(base64) {
            const paddingLength = (4 - base64.length % 4) % 4;
            const paddedBase64 = base64 + '='.repeat(paddingLength);
            try {
                const binaryString = atob(paddedBase64);
                const len = binaryString.length;
                const bytes = new Uint8Array(len);
                for (let i = 0; i < len; i++) {
                    bytes[i] = binaryString.charCodeAt(i);
                }
                return bytes;
            } catch (error) {
                console.error('Error while decoding base64:', error);
                throw error;
            }
        }

    }

    /**
     * 
     * @param {String} jwtPayloadPartAsJson - the payload part of a JWT string
     * @returns {Object | null} an Object corresponding passed JSON string Payload payload part.
     */
    static parseJwtPayloadStringToObject(jwtPayloadPartAsJson) {
        try {
            return JSON.parse(jwtPayloadPartAsJson);
        } catch (error) {
            console.error('Error while parsing JWT:', error);
            return null;
        }
    }

    /**
     * 
     * @param {String} jwtTokenAsJwtString - the JWT string
     * @returns {Number} UTC +0 in milliseconds, the expiration time in UTC Unix time format, or 0 if the JWT
     *  is invalid or has no expiration time
     */
    static getExpirationTimeUtc(jwtTokenAsJwtString) {
        if (!jwtTokenAsJwtString) {
            return 0;
        }
        const jsonPayload = TokenParser.parseJwtToJsonPayloadAsString(jwtTokenAsJwtString);
        if (!jsonPayload) {
            return 0;
        }
        const jwtObject = TokenParser.parseJwtPayloadStringToObject(jsonPayload);
        if (!jwtObject || jwtObject.exp === undefined) {
            return 0;
        }
        return jwtObject.exp;
    }
}

export default TokenParser;