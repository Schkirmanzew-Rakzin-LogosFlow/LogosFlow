/**
  @author: Dmytro Shkirmantsev <shkirmantsev@gmail.com>
*/
class PageStateStorage {

  static STATE_KEY = "pageState";

  static storeOauth2State(state) {
    window.localStorage.setItem(PageStateStorage.STATE_KEY, state);

    return state;
  }

  static retrieveOauth2State() {
    return window.localStorage.getItem(PageStateStorage.STATE_KEY);
  }

  static clear() {
    window.localStorage.removeItem(PageStateStorage.STATE_KEY);
  }
};

export default PageStateStorage;
