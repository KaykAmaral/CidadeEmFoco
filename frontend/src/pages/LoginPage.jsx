import { LoaderCircle } from 'lucide-react'
import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router'
import Button from '../components/ui/Button'
import useAuth from '../hooks/useAuth'

function LoginPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const { login } = useAuth()
  const [form, setForm] = useState({
    email: location.state?.email ?? '',
    password: '',
  })
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})
  const [isSubmitting, setIsSubmitting] = useState(false)

  function handleChange(event) {
    const { name, value } = event.target
    setForm((currentForm) => ({ ...currentForm, [name]: value }))
    setError('')
    setFieldErrors({})
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setFieldErrors({})
    setIsSubmitting(true)

    try {
      const user = await login(form)
      const defaultDestination = user.role === 'ADMIN' ? '/admin' : '/app'
      const destination = location.state?.from?.pathname ?? defaultDestination
      navigate(destination, { replace: true })
    } catch (requestError) {
      setError(requestError.message)
      setFieldErrors(requestError.fieldErrors ?? {})
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="auth-card">
      <div className="auth-card__heading">
        <span>Acesso</span>
        <h2>Entre na sua conta</h2>
        <p>Use seu e-mail e senha para acessar o Cidade em Foco.</p>
      </div>

      {location.state?.registered && (
        <div className="form-message form-message--success" role="status">
          Cadastro realizado. Agora você já pode entrar.
        </div>
      )}

      {error && (
        <div className="form-message form-message--error" role="alert">
          {error}
        </div>
      )}

      <form className="auth-form" onSubmit={handleSubmit}>
        <div className="form-field">
          <label htmlFor="login-email">E-mail</label>
          <input
            aria-describedby={fieldErrors.email ? 'login-email-error' : undefined}
            aria-invalid={Boolean(fieldErrors.email)}
            autoComplete="email"
            className="form-control"
            id="login-email"
            maxLength={254}
            name="email"
            onChange={handleChange}
            placeholder="seuemail@exemplo.com"
            required
            type="email"
            value={form.email}
          />
          {fieldErrors.email && (
            <small className="field-error" id="login-email-error">
              {fieldErrors.email}
            </small>
          )}
        </div>

        <div className="form-field">
          <label htmlFor="login-password">Senha</label>
          <input
            aria-describedby={
              fieldErrors.password ? 'login-password-error' : undefined
            }
            aria-invalid={Boolean(fieldErrors.password)}
            autoComplete="current-password"
            className="form-control"
            id="login-password"
            name="password"
            onChange={handleChange}
            placeholder="Digite sua senha"
            required
            type="password"
            value={form.password}
          />
          {fieldErrors.password && (
            <small className="field-error" id="login-password-error">
              {fieldErrors.password}
            </small>
          )}
        </div>

        <Button
          className="auth-form__submit"
          disabled={isSubmitting}
          type="submit"
        >
          {isSubmitting && (
            <LoaderCircle
              className="button__spinner"
              size={18}
              aria-hidden="true"
            />
          )}
          {isSubmitting ? 'Entrando...' : 'Entrar'}
        </Button>
      </form>

      <p className="auth-card__footer">
        Ainda não tem conta? <Link to="/cadastro">Cadastre-se</Link>
      </p>
    </div>
  )
}

export default LoginPage
