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
import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router'
import Button from '../components/ui/Button'
import useAuth from '../hooks/useAuth'
import { getActiveCities } from '../services/cityService'
import './LoginPage.css'

const initialForm = {
  name: '',
  email: '',
  password: '',
  cpf: '',
  whatsappPhone: '',
  whatsappConsentGiven: false,
  street: '',
  addressNumber: '',
  addressComplement: '',
  neighborhood: '',
  postalCode: '',
  cityIbgeCode: '',
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
  const [isSuccess, setIsSuccess] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  const [cities, setCities] = useState([])
  const [citiesError, setCitiesError] = useState('')
  const submittingRef = useRef(false)

  useEffect(() => {
    let isCurrent = true
    getActiveCities()
      .then((availableCities) => {
        if (!isCurrent) return
        setCities(availableCities)
        if (availableCities.length === 1) {
          setForm((current) => ({ ...current, cityIbgeCode: availableCities[0].ibgeCode }))
        }
      })
      .catch((requestError) => {
        if (isCurrent) setCitiesError(requestError.message)
      })
    return () => {
      isCurrent = false
    }
  }, [])

  function handleChange(event) {
    const { checked, name, type, value } = event.target
    setForm((currentForm) => ({
      ...currentForm,
      [name]: type === 'checkbox' ? checked : value,
    }))
    setError('')
    setFieldErrors((currentErrors) => ({ ...currentErrors, [name]: undefined }))
  }

  async function handleSubmit(event) {
    event.preventDefault()
    if (submittingRef.current) return

    submittingRef.current = true
    setError('')
    setFieldErrors({})
    setIsSubmitting(true)
    setIsSuccess(false)

    try {
      const user = await register(form)
      setIsSuccess(true)
      navigate(user.role === 'ADMIN' ? '/admin' : '/app', {
        replace: true,
        state: { registered: true },
      })
    } catch (requestError) {
      const requestFieldErrors = requestError.fieldErrors ?? {}
      setFieldErrors(requestFieldErrors)
      setError(Object.keys(requestFieldErrors).length === 0 ? requestError.message : '')
    } finally {
      submittingRef.current = false
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

          {isSuccess && (
            <div className="form-message form-message--success" role="status">
              Cadastro concluído. Entrando na sua conta...
            </div>
          )}

          <form className="auth-form" onSubmit={handleSubmit} aria-busy={isSubmitting}>
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

            <div className="register-grid">
              <div className="form-field">
                <label htmlFor="register-cpf">CPF</label>
                <div className="login-input">
                  <input aria-invalid={Boolean(fieldErrors.cpf)} autoComplete="off" id="register-cpf" maxLength={14} name="cpf" onChange={handleChange} placeholder="000.000.000-00" required value={form.cpf} />
                </div>
                {fieldErrors.cpf && <small className="field-error">{fieldErrors.cpf}</small>}
              </div>
              <div className="form-field">
                <label htmlFor="register-whatsapp">WhatsApp</label>
                <div className="login-input">
                  <input aria-invalid={Boolean(fieldErrors.whatsappPhone)} autoComplete="tel" id="register-whatsapp" maxLength={30} name="whatsappPhone" onChange={handleChange} placeholder="+5513999999999" required type="tel" value={form.whatsappPhone} />
                </div>
                {fieldErrors.whatsappPhone && <small className="field-error">{fieldErrors.whatsappPhone}</small>}
              </div>
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

            <div className="register-grid">
              <div className="form-field form-field--wide">
                <label htmlFor="register-street">Logradouro</label>
                <div className="login-input">
                  <input aria-invalid={Boolean(fieldErrors.street)} autoComplete="street-address" id="register-street" maxLength={150} name="street" onChange={handleChange} placeholder="Rua ou avenida" required value={form.street} />
                </div>
                {fieldErrors.street && <small className="field-error">{fieldErrors.street}</small>}
              </div>
              <div className="form-field">
                <label htmlFor="register-number">Número</label>
                <div className="login-input">
                  <input aria-invalid={Boolean(fieldErrors.addressNumber)} id="register-number" maxLength={20} name="addressNumber" onChange={handleChange} placeholder="123" required value={form.addressNumber} />
                </div>
                {fieldErrors.addressNumber && <small className="field-error">{fieldErrors.addressNumber}</small>}
              </div>
              <div className="form-field">
                <label htmlFor="register-complement">Complemento</label>
                <div className="login-input">
                  <input aria-invalid={Boolean(fieldErrors.addressComplement)} id="register-complement" maxLength={100} name="addressComplement" onChange={handleChange} placeholder="Opcional" value={form.addressComplement} />
                </div>
                {fieldErrors.addressComplement && <small className="field-error">{fieldErrors.addressComplement}</small>}
              </div>
              <div className="form-field">
                <label htmlFor="register-neighborhood">Bairro</label>
                <div className="login-input">
                  <input aria-invalid={Boolean(fieldErrors.neighborhood)} id="register-neighborhood" maxLength={100} name="neighborhood" onChange={handleChange} required value={form.neighborhood} />
                </div>
                {fieldErrors.neighborhood && <small className="field-error">{fieldErrors.neighborhood}</small>}
              </div>
              <div className="form-field">
                <label htmlFor="register-postal-code">CEP</label>
                <div className="login-input">
                  <input aria-invalid={Boolean(fieldErrors.postalCode)} autoComplete="postal-code" id="register-postal-code" maxLength={9} name="postalCode" onChange={handleChange} placeholder="00000-000" required value={form.postalCode} />
                </div>
                {fieldErrors.postalCode && <small className="field-error">{fieldErrors.postalCode}</small>}
              </div>
              <div className="form-field">
                <label htmlFor="register-city">Cidade</label>
                <div className="login-input">
                  <select aria-invalid={Boolean(fieldErrors.cityIbgeCode)} disabled={cities.length === 0} id="register-city" name="cityIbgeCode" onChange={handleChange} required value={form.cityIbgeCode}>
                    <option value="">Selecione</option>
                    {cities.map((city) => <option key={city.ibgeCode} value={city.ibgeCode}>{city.name} — {city.stateCode}</option>)}
                  </select>
                </div>
                {fieldErrors.cityIbgeCode && <small className="field-error">{fieldErrors.cityIbgeCode}</small>}
                {citiesError && <small className="field-error">{citiesError}</small>}
              </div>
            </div>

            <label className="register-consent">
              <input checked={form.whatsappConsentGiven} name="whatsappConsentGiven" onChange={handleChange} required type="checkbox" />
              <span>Autorizo o recebimento de alertas climáticos pelo WhatsApp.</span>
            </label>
            {fieldErrors.whatsappConsentGiven && <small className="field-error">{fieldErrors.whatsappConsentGiven}</small>}

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
