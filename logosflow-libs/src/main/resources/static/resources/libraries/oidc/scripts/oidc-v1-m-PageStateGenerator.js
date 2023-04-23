/**
  @author: Dmytro Shkirmantsev <shkirmantsev@gmail.com>
*/
class PageStateGenerator {

  /**
   * 
   * @returns {String} a random string of 32 characters
   */
  static generateState() {
    return PageStateGenerator.generateRandomString(32);
  }

  /**
   * 
   * @param {Number} length 
   * @returns {String}
   */
  static generateRandomString(length) {
    const characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    const charactersLength = characters.length;
    const result = [];

    for (let i = 0; i < length; i++) {
      const randomIndex = Math.floor(Math.random() * charactersLength);
      result.push(characters.charAt(randomIndex));
    }

    return result.join("");
  }
}

export default PageStateGenerator;
