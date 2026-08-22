import { Link } from 'react-router'
import Button from '../components/ui/Button'

function LoginPage() {
  function handleSubmit(event) {
    event.preventDefault()
  }

  return (
    <div className="auth-card">
      <div className="auth-card__heading">
        <span>Acesso</span>
        <h2>Entre na sua conta</h2>
        <p>Use seu e-mail e senha para acessar o Cidade em Foco.</p>
      </div>

      <form className="auth-form" onSubmit={handleSubmit}>
        <div className="form-field">
          <label htmlFor="login-email">E-mail</label>
          <input
            autoComplete="email"
            className="form-control"
            id="login-email"
            name="email"
            placeholder="seuemail@exemplo.com"
            type="email"
          />
        </div>

        <div className="form-field">
          <label htmlFor="login-password">Senha</label>
          <input
            autoComplete="current-password"
            className="form-control"
            id="login-password"
            name="password"
            placeholder="Digite sua senha"
            type="password"
          />
        </div>

        <Button className="auth-form__submit" type="submit">
          Entrar
        </Button>
      </form>

      <p className="auth-card__footer">
        Ainda não tem conta? <Link to="/cadastro">Cadastre-se</Link>
      </p>

      <p className="development-hint">
        A autenticação será conectada ao backend na etapa própria.
      </p>
    </div>
  )
}

export default LoginPage
