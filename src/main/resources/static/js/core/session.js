/** Keeps the bearer token in this tab session; logout removes it from browser storage. */
const STORAGE_KEY = "hotels-token";
export const session = {
  get token() {
    return sessionStorage.getItem(STORAGE_KEY);
  },
  save(token) {
    sessionStorage.setItem(STORAGE_KEY, token);
  },
  clear() {
    sessionStorage.removeItem(STORAGE_KEY);
  },
};
