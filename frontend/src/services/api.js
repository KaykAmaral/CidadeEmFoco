const API_URL = import.meta.env.VITE_API_URL

class ApiError extends Error {
  constructor(message, status = 0, fieldErrors = {}) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

async function apiRequest(path, options = {}) {
  const { token, headers: customHeaders, ...fetchOptions } = options
  const isFormData = fetchOptions.body instanceof FormData
  const headers = new Headers(customHeaders)

  if (fetchOptions.body && !isFormData) {
    headers.set('Content-Type', 'application/json')
  }

  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  let response

  try {
    response = await fetch(`${API_URL}${path}`, {
      ...fetchOptions,
      headers,
    })
  } catch {
    throw new ApiError(
      'Não foi possível conectar ao servidor. Verifique se o backend está em execução.',
    )
  }

  const contentType = response.headers.get('content-type') ?? ''
  const data = contentType.includes('application/json')
    ? await response.json()
    : null

  if (!response.ok) {
    throw new ApiError(
      data?.message ?? 'Não foi possível concluir a solicitação.',
      response.status,
      data?.fieldErrors ?? {},
    )
  }

  return data
}

export { ApiError, apiRequest }
