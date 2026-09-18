import { AlertCircle, Inbox, LoaderCircle } from 'lucide-react'

const feedbackConfig = {
  loading: {
    icon: LoaderCircle,
    title: 'Carregando',
    message: 'Aguarde enquanto buscamos as informações.',
  },
  empty: {
    icon: Inbox,
    title: 'Nenhum resultado',
    message: 'Não encontramos itens para exibir neste momento.',
  },
  error: {
    icon: AlertCircle,
    title: 'Não foi possível carregar',
    message: 'Tente novamente em alguns instantes.',
  },
}

function FeedbackState({ type = 'empty', title, message }) {
  const config = feedbackConfig[type] ?? feedbackConfig.empty
  const Icon = config.icon

  return (
    <div
      className={`feedback-state feedback-state--${type}`}
      role={type === 'error' ? 'alert' : 'status'}
    >
      <Icon className="feedback-state__icon" size={24} aria-hidden="true" />
      <div>
        <strong>{title ?? config.title}</strong>
        <p>{message ?? config.message}</p>
      </div>
    </div>
  )
}

export default FeedbackState
