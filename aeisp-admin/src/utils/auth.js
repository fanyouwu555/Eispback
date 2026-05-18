const TokenKey = 'aeisp-access-token'
const RefreshTokenKey = 'aeisp-refresh-token'

export function getToken() {
  return localStorage.getItem(TokenKey)
}

export function setToken(token) {
  localStorage.setItem(TokenKey, token)
}

export function removeToken() {
  localStorage.removeItem(TokenKey)
}

export function getRefreshToken() {
  return localStorage.getItem(RefreshTokenKey)
}

export function setRefreshToken(token) {
  localStorage.setItem(RefreshTokenKey, token)
}

export function removeRefreshToken() {
  localStorage.removeItem(RefreshTokenKey)
}
