import {
  BarChart3,
  Bell,
  Eye,
  EyeOff,
  LoaderCircle,
  LockKeyhole,
  Mail,
  ShieldCheck,
  UserRound,
  Users,
} from 'lucide-react'
import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import Button from '../components/ui/Button'
import useAuth from '../hooks/useAuth'
import './LoginPage.css'

const initialForm = {
  name: '',
  email: '',
  password: '',
}

const benefits = [
  { icon: Users, title: 'Participe', description: 'da sua cidade' },
  { icon: Bell, title: 'Alerte', description: 'problemas urbanos' },
  { icon: BarChart3, title: 'Acompanhe', description: 'as ações' },
  { icon: ShieldCheck, title: 'Contribua', description: 'para uma cidade melhor' },
]

function RegisterPage() {
  const navigate = useNavigate()
  const { register } = useAuth()
  const [form, setForm] = useState(initialForm)
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [showPassword, setShowPassword] = useState(false)

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
    <main className="login-page login-page--register">
      <section
        className="login-hero"
        role="img"
        aria-label="Vista aérea da orla, praia e área urbana de Praia Grande"
      >
        <div className="login-hero__brand">
          <img
            src="/brand/cidade-em-foco-app-icon.png"
            alt="Cidade em Foco — Nossa cidade, nosso olhar"
          />
        </div>

        <div className="login-hero__message">
          <span>Praia Grande — SP</span>
          <h1>
            Informação e<br />
            participação para<br />
            cuidar da cidade.
          </h1>
          <p>
            Registre ocorrências urbanas e acompanhe<br />
            alertas climáticos em um único lugar.
          </p>
        </div>

        <div className="login-benefits" aria-label="Benefícios do Cidade em Foco">
          {benefits.map(({ icon: Icon, title, description }) => (
            <div className="login-benefit" key={title}>
              <span className="login-benefit__icon" aria-hidden="true">
                <Icon size={20} strokeWidth={1.8} />
              </span>
              <div>
                <strong>{title}</strong>
                <span>{description}</span>
              </div>
            </div>
          ))}
        </div>
      </section>

      <section className="login-panel">
        <svg className="login-panel__curves" viewBox="0 0 320 180" aria-hidden="true">
          <path d="M52 -18c68 60 102 35 154 72 39 28 48 73 126 85" />
          <path d="M88 -23c55 50 91 28 143 65 40 28 48 67 105 79" />
          <path d="M126 -26c41 38 76 24 124 56 37 24 45 55 89 68" />
        </svg>

        <div className="login-form-wrap">
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
              <div className="login-input">
                <UserRound size={20} aria-hidden="true" />
                <input
                  aria-describedby={fieldErrors.name ? 'register-name-error' : undefined}
                  aria-invalid={Boolean(fieldErrors.name)}
                  autoComplete="name"
                  id="register-name"
                  maxLength={100}
                  minLength={2}
                  name="name"
                  onChange={handleChange}
                  placeholder="Seu nome completo"
                  required
                  value={form.name}
                />
              </div>
              {fieldErrors.name && (
                <small className="field-error" id="register-name-error">
                  {fieldErrors.name}
                </small>
              )}
            </div>

            <div className="form-field">
              <label htmlFor="register-email">E-mail</label>
              <div className="login-input">
                <Mail size={20} aria-hidden="true" />
                <input
                  aria-describedby={fieldErrors.email ? 'register-email-error' : undefined}
                  aria-invalid={Boolean(fieldErrors.email)}
                  autoComplete="email"
                  id="register-email"
                  maxLength={254}
                  name="email"
                  onChange={handleChange}
                  placeholder="seuemail@exemplo.com"
                  required
                  type="email"
                  value={form.email}
                />
              </div>
              {fieldErrors.email && (
                <small className="field-error" id="register-email-error">
                  {fieldErrors.email}
                </small>
              )}
            </div>

            <div className="form-field">
              <label htmlFor="register-password">Senha</label>
              <div className="login-input">
                <LockKeyhole size={20} aria-hidden="true" />
                <input
                  aria-describedby={fieldErrors.password ? 'register-password-error' : 'register-password-help'}
                  aria-invalid={Boolean(fieldErrors.password)}
                  autoComplete="new-password"
                  id="register-password"
                  maxLength={72}
                  minLength={8}
                  name="password"
                  onChange={handleChange}
                  placeholder="Crie uma senha"
                  required
                  type={showPassword ? 'text' : 'password'}
                  value={form.password}
                />
                <button
                  className="login-input__toggle"
                  type="button"
                  onClick={() => setShowPassword((visible) => !visible)}
                  aria-label={showPassword ? 'Ocultar senha' : 'Mostrar senha'}
                  aria-pressed={showPassword}
                >
                  {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
                </button>
              </div>
              {fieldErrors.password ? (
                <small className="field-error" id="register-password-error">
                  {fieldErrors.password}
                </small>
              ) : (
                <small id="register-password-help">Use entre 8 e 72 caracteres.</small>
              )}
            </div>

            <Button className="auth-form__submit" disabled={isSubmitting} type="submit">
              {isSubmitting && (
                <LoaderCircle className="button__spinner" size={18} aria-hidden="true" />
              )}
              {isSubmitting ? 'Criando conta...' : 'Criar conta'}
            </Button>
          </form>

          <p className="auth-card__footer">
            Já possui cadastro? <Link to="/login">Entrar</Link>
          </p>
        </div>

        <svg className="login-panel__skyline" viewBox="0 0 900 150" aria-hidden="true">
          <path d="M0 120c84-16 121 20 203 1 72-17 109-7 171 5 75 15 125-15 192-6 85 12 144 29 334 3" />
          <path d="M0 136c104-13 163 12 250 1 97-13 164 7 246 3 134-7 209-4 404 2" />
          <path d="M80 116v-24h27v22m12-2V75h42v40m12 3V93h30v25m347 0V82h34v35m13 2V65h43v56m13-2V96h31v24M129 75v-9m10 9v-16m10 16v-11m417 18V70m14 12V62m14 20V72" />
          <path d="M338 126v-35c0-22 15-38 34-38s34 16 34 38v36m-68-34h68M720 124v-42m0 13c-20-8-27-22-25-39 18 8 26 21 25 39Zm1-6c16-7 22-18 20-31-14 6-21 16-20 31Z" />
          <path d="M776 45c8-8 16-8 24 0 8-8 16-8 24 0" />
        </svg>
      </section>
    </main>
  )
}

export default RegisterPage
