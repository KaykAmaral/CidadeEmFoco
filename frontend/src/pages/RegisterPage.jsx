import { Link } from 'react-router'
import Button from '../components/ui/Button'

function RegisterPage() {
  function handleSubmit(event) {
    event.preventDefault()
  }

  return (
    <div className="auth-card">
      <div className="auth-card__heading">
        <span>Cadastro</span>
        <h2>Crie sua conta</h2>
        <p>O cadastro público cria somente usuários com perfil cidadão.</p>
      </div>

      <form className="auth-form" onSubmit={handleSubmit}>
        <div className="form-field">
          <label htmlFor="register-name">Nome</label>
          <input
            autoComplete="name"
            className="form-control"
            id="register-name"
            name="name"
            placeholder="Seu nome completo"
          />
        </div>

        <div className="form-field">
          <label htmlFor="register-email">E-mail</label>
          <input
            autoComplete="email"
            className="form-control"
            id="register-email"
            name="email"
            placeholder="seuemail@exemplo.com"
            type="email"
          />
        </div>

        <div className="form-field">
          <label htmlFor="register-password">Senha</label>
          <input
            autoComplete="new-password"
            className="form-control"
            id="register-password"
            name="password"
            placeholder="Crie uma senha"
            type="password"
          />
        </div>

        <Button className="auth-form__submit" type="submit">
          Criar conta
        </Button>
      </form>

      <p className="auth-card__footer">
        Já possui cadastro? <Link to="/login">Entrar</Link>
      </p>

      <p className="development-hint">
        O envio do cadastro será conectado ao backend na etapa de autenticação.
      </p>
    </div>
  )
}

export default RegisterPage
