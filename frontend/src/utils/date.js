const dateTimeFormatter = new Intl.DateTimeFormat('pt-BR', {
  dateStyle: 'short',
  timeStyle: 'short',
})

function formatDateTime(value) {
  if (!value) {
    return 'Data não informada'
  }

  return dateTimeFormatter.format(new Date(value))
}

export { formatDateTime }
