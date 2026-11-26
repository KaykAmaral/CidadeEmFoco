import { LoaderCircle, Mail } from 'lucide-react'
import { useState } from 'react'
import { Link } from 'react-router'
import Button from '../components/ui/Button'
import { forgotPasswordRequest } from '../services/authService'
import './PasswordRecoveryPage.css'

function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [sent, setSent] = useState(false)

  async function submit(event) {
    event.preventDefault()
    setError('')
    setFieldErrors({})
    setIsSubmitting(true)
    try {
      await forgotPasswordRequest(email)
      setSent(true)
    } catch (requestError) {
      setError(requestError.message)
      setFieldErrors(requestError.fieldErrors ?? {})
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section className="auth-card password-recovery">
      <div className="auth-card__heading">
        <span>Recuperação de acesso</span>
        <h2>Esqueceu sua senha?</h2>
        <p>Informe seu e-mail. Se ele estiver cadastrado, enviaremos um link válido por 30 minutos.</p>
      </div>

      {sent ? (
        <div className="password-recovery__result">
          <div className="form-message form-message--success" role="status">
            Solicitação recebida. Confira sua caixa de entrada e também a pasta de spam.
          </div>
          <Link className="button button--primary" to="/login">Voltar para entrar</Link>
        </div>
      ) : (
        <form className="auth-form" onSubmit={submit}>
          {error && <div className="form-message form-message--error" role="alert">{error}</div>}
          <div className="form-field">
            <label htmlFor="forgot-email">E-mail</label>
            <div className="login-input">
              <Mail size={20} aria-hidden="true" />
              <input autoComplete="email" id="forgot-email" maxLength={254} name="email" onChange={(event) => { setEmail(event.target.value); setError(''); setFieldErrors({}) }} placeholder="seuemail@exemplo.com" required type="email" value={email} />
            </div>
            {fieldErrors.email && <small className="field-error">{fieldErrors.email}</small>}
          </div>
          <Button className="auth-form__submit" disabled={isSubmitting} type="submit">
            {isSubmitting && <LoaderCircle className="button__spinner" size={18} aria-hidden="true" />}
            {isSubmitting ? 'Enviando...' : 'Enviar link de recuperação'}
          </Button>
          <Link className="password-recovery__back" to="/login">Voltar para entrar</Link>
        </form>
      )}
    </section>
  )
}

export default ForgotPasswordPage
