import { LoaderCircle } from 'lucide-react'
import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import Button from '../components/ui/Button'
import useAuth from '../hooks/useAuth'

const initialForm = {
  name: '',
  email: '',
  password: '',
}

function RegisterPage() {
  const navigate = useNavigate()
  const { register } = useAuth()
  const [form, setForm] = useState(initialForm)
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
      await register(form)
      navigate('/login', {
        replace: true,
        state: { registered: true, email: form.email },
      })
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
        <span>Cadastro</span>
        <h2>Crie sua conta</h2>
        <p>O cadastro público cria somente usuários com perfil cidadão.</p>
      </div>

      {error && (
        <div className="form-message form-message--error" role="alert">
          {error}
        </div>
      )}

      <form className="auth-form" onSubmit={handleSubmit}>
        <div className="form-field">
          <label htmlFor="register-name">Nome</label>
          <input
            aria-describedby={
              fieldErrors.name ? 'register-name-error' : undefined
            }
            aria-invalid={Boolean(fieldErrors.name)}
            autoComplete="name"
            className="form-control"
            id="register-name"
            maxLength={100}
            minLength={2}
            name="name"
            onChange={handleChange}
            placeholder="Seu nome completo"
            required
            value={form.name}
          />
          {fieldErrors.name && (
            <small className="field-error" id="register-name-error">
              {fieldErrors.name}
            </small>
          )}
        </div>

        <div className="form-field">
          <label htmlFor="register-email">E-mail</label>
          <input
            aria-describedby={
              fieldErrors.email ? 'register-email-error' : undefined
            }
            aria-invalid={Boolean(fieldErrors.email)}
            autoComplete="email"
            className="form-control"
            id="register-email"
            maxLength={254}
            name="email"
            onChange={handleChange}
            placeholder="seuemail@exemplo.com"
            required
            type="email"
            value={form.email}
          />
          {fieldErrors.email && (
            <small className="field-error" id="register-email-error">
              {fieldErrors.email}
            </small>
          )}
        </div>

        <div className="form-field">
          <label htmlFor="register-password">Senha</label>
          <input
            aria-describedby={
              fieldErrors.password
                ? 'register-password-error'
                : 'register-password-help'
            }
            aria-invalid={Boolean(fieldErrors.password)}
            autoComplete="new-password"
            className="form-control"
            id="register-password"
            maxLength={72}
            minLength={8}
            name="password"
            onChange={handleChange}
            placeholder="Crie uma senha"
            required
            type="password"
            value={form.password}
          />
          {fieldErrors.password ? (
            <small className="field-error" id="register-password-error">
              {fieldErrors.password}
            </small>
          ) : (
            <small id="register-password-help">Use entre 8 e 72 caracteres.</small>
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
          {isSubmitting ? 'Criando conta...' : 'Criar conta'}
        </Button>
      </form>

      <p className="auth-card__footer">
        Já possui cadastro? <Link to="/login">Entrar</Link>
      </p>
    </div>
  )
}

export default RegisterPage
