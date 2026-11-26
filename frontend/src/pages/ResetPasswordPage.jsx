import { Eye, EyeOff, LoaderCircle, LockKeyhole } from 'lucide-react'
import { useState } from 'react'
import { Link, useSearchParams } from 'react-router'
import Button from '../components/ui/Button'
import { resetPasswordRequest } from '../services/authService'
import './PasswordRecoveryPage.css'

function ResetPasswordPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token') ?? ''
  const [form, setForm] = useState({ newPassword: '', confirmation: '' })
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [completed, setCompleted] = useState(false)
  const [showPassword, setShowPassword] = useState(false)

  function change(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }))
    setError('')
    setFieldErrors({})
  }

  async function submit(event) {
    event.preventDefault()
    setError('')
    setFieldErrors({})
    if (form.newPassword !== form.confirmation) {
      setFieldErrors({ confirmation: 'As senhas não coincidem.' })
      return
    }
    if (!token) {
      setError('O link de recuperação está incompleto. Solicite um novo link.')
      return
    }

    setIsSubmitting(true)
    try {
      await resetPasswordRequest(token, form.newPassword)
      setCompleted(true)
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
        <span>Nova senha</span>
        <h2>Redefina seu acesso</h2>
        <p>Crie uma nova senha com pelo menos oito caracteres.</p>
      </div>

      {completed ? (
        <div className="password-recovery__result">
          <div className="form-message form-message--success" role="status">Senha redefinida com sucesso. Você já pode entrar novamente.</div>
          <Link className="button button--primary" to="/login">Entrar na minha conta</Link>
        </div>
      ) : (
        <form className="auth-form" onSubmit={submit}>
          {error && <div className="form-message form-message--error" role="alert">{error}</div>}
          <div className="form-field">
            <label htmlFor="reset-password">Nova senha</label>
            <div className="login-input">
              <LockKeyhole size={20} aria-hidden="true" />
              <input autoComplete="new-password" id="reset-password" maxLength={72} minLength={8} name="newPassword" onChange={change} required type={showPassword ? 'text' : 'password'} value={form.newPassword} />
              <button aria-label={showPassword ? 'Ocultar senha' : 'Mostrar senha'} className="login-input__toggle" onClick={() => setShowPassword((current) => !current)} type="button">{showPassword ? <EyeOff size={20} /> : <Eye size={20} />}</button>
            </div>
            {fieldErrors.newPassword && <small className="field-error">{fieldErrors.newPassword}</small>}
          </div>
          <div className="form-field">
            <label htmlFor="reset-confirmation">Confirmar nova senha</label>
            <div className="login-input"><LockKeyhole size={20} aria-hidden="true" /><input autoComplete="new-password" id="reset-confirmation" maxLength={72} minLength={8} name="confirmation" onChange={change} required type={showPassword ? 'text' : 'password'} value={form.confirmation} /></div>
            {fieldErrors.confirmation && <small className="field-error">{fieldErrors.confirmation}</small>}
          </div>
          <Button className="auth-form__submit" disabled={isSubmitting} type="submit">
            {isSubmitting && <LoaderCircle className="button__spinner" size={18} aria-hidden="true" />}
            {isSubmitting ? 'Salvando...' : 'Redefinir senha'}
          </Button>
          <Link className="password-recovery__back" to="/login">Voltar para entrar</Link>
        </form>
      )}
    </section>
  )
}

export default ResetPasswordPage
