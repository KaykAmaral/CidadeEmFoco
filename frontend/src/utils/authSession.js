function createSession(response, now = Date.now()) {
  return {
    token: response.token,
    tokenType: response.tokenType,
    expiresAt: now + response.expiresInSeconds * 1000,
    user: response.user,
  }
}

export { createSession }
