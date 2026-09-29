// Only a retry interval: marker retention and normal refresh frequency come from the API.
const INITIAL_RETRY_MS = 5000

function startMapPolling(load, onSuccess, onError) {
  const controller = new AbortController()
  let timer
  let running = false
  let delay = INITIAL_RETRY_MS

  async function refresh() {
    if (controller.signal.aborted || running) return
    clearTimeout(timer)
    running = true
    try {
      const response = await load(controller.signal)
      if (controller.signal.aborted) return
      delay = response.refreshAfterMillis
      onSuccess(response.occurrences)
    } catch (error) {
      if (!controller.signal.aborted) onError(error)
    } finally {
      running = false
      if (!controller.signal.aborted) timer = setTimeout(refresh, delay)
    }
  }

  // Defer the initial request so StrictMode cleanup can prevent duplicate requests.
  timer = setTimeout(refresh, 0)
  return {
    refresh,
    stop() {
      clearTimeout(timer)
      controller.abort()
    },
  }
}

export { startMapPolling }
