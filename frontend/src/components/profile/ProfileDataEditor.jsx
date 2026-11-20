import { useEffect, useState } from 'react'
import Button from '../ui/Button'
import { getActiveCities } from '../../services/cityService'
import { getMyProfile, updateMyProfile } from '../../services/userService'

const fields = [
  ['name', 'Nome', 'text'], ['cpf', 'CPF', 'text'], ['whatsappPhone', 'WhatsApp com DDD', 'tel'],
  ['street', 'Rua', 'text'], ['addressNumber', 'Número', 'text'],
  ['addressComplement', 'Complemento', 'text'], ['neighborhood', 'Bairro', 'text'],
  ['postalCode', 'CEP', 'text'],
]

function toForm(profile) {
  return {
    name: profile?.name ?? '', cpf: profile?.cpf ?? '', whatsappPhone: profile?.whatsappPhone ?? '',
    whatsappConsentGiven: profile?.whatsappNotificationsEnabled ?? false,
    street: profile?.address?.street ?? '', addressNumber: profile?.address?.number ?? '',
    addressComplement: profile?.address?.complement ?? '', neighborhood: profile?.address?.neighborhood ?? '',
    postalCode: profile?.address?.postalCode ?? '', cityIbgeCode: profile?.city?.ibgeCode ?? '',
  }
}

function ProfileDataEditor({ logout, token }) {
  const [profile, setProfile] = useState(null)
  const [cities, setCities] = useState([])
  const [form, setForm] = useState(toForm())
  const [editing, setEditing] = useState(false)
  const [busy, setBusy] = useState(true)
  const [error, setError] = useState('')
  const [fieldErrors, setFieldErrors] = useState({})
  const [success, setSuccess] = useState('')

  useEffect(() => {
    Promise.all([getMyProfile(token), getActiveCities()])
      .then(([data, availableCities]) => { setProfile(data); setForm(toForm(data)); setCities(availableCities) })
      .catch((requestError) => requestError.status === 401 ? logout() : setError(requestError.message))
      .finally(() => setBusy(false))
  }, [logout, token])

  function change(event) {
    const { checked, name, type, value } = event.target
    setForm((current) => ({ ...current, [name]: type === 'checkbox' ? checked : value }))
    setFieldErrors((current) => ({ ...current, [name]: undefined }))
  }

  async function submit(event) {
    event.preventDefault(); setBusy(true); setError(''); setSuccess(''); setFieldErrors({})
    try {
      const updated = await updateMyProfile(token, form)
      setProfile(updated); setForm(toForm(updated)); setEditing(false); setSuccess('Dados cadastrais atualizados com sucesso.')
    } catch (requestError) {
      if (requestError.status === 401) return logout()
      setFieldErrors(requestError.fieldErrors ?? {})
      if (!Object.keys(requestError.fieldErrors ?? {}).length) setError(requestError.message)
    } finally { setBusy(false) }
  }

  if (busy && !profile) return <p role="status">Carregando dados cadastrais...</p>
  if (!profile) return <div className="form-message form-message--error" role="alert">{error}</div>

  return (
    <section className="profile-preferences" aria-labelledby="profile-data-title">
      <div className="profile-preferences__heading"><div><span>Cadastro</span><h3 id="profile-data-title">Dados pessoais e endereço</h3></div>
        {!editing && <Button onClick={() => { setEditing(true); setError(''); setSuccess('') }} variant="outline">Editar perfil</Button>}
      </div>
      {success && <div className="form-message form-message--success" role="status">{success}</div>}
      {error && <div className="form-message form-message--error" role="alert">{error}</div>}
      {editing ? <form className="profile-preferences__form" onSubmit={submit}>
        {fields.map(([name, label, type]) => <div className="form-field" key={name}><label htmlFor={`profile-${name}`}>{label}</label><input aria-invalid={Boolean(fieldErrors[name])} className="form-control" id={`profile-${name}`} name={name} onChange={change} required={!['addressComplement'].includes(name)} type={type} value={form[name]} />{fieldErrors[name] && <small className="field-error">{fieldErrors[name]}</small>}</div>)}
        <div className="form-field"><label htmlFor="profile-city">Cidade/UF</label><select className="form-control" id="profile-city" name="cityIbgeCode" onChange={change} required value={form.cityIbgeCode}>{cities.map((city) => <option key={city.ibgeCode} value={city.ibgeCode}>{city.name}/{city.state}</option>)}</select>{fieldErrors.cityIbgeCode && <small className="field-error">{fieldErrors.cityIbgeCode}</small>}</div>
        <label className="profile-consent"><input checked={form.whatsappConsentGiven} name="whatsappConsentGiven" onChange={change} type="checkbox" /><span>Autorizo notificações pelo WhatsApp.</span></label>
        <div className="profile-preferences__actions"><Button disabled={busy} type="submit">{busy ? 'Salvando...' : 'Salvar'}</Button><Button onClick={() => { setEditing(false); setForm(toForm(profile)) }} type="button" variant="outline">Cancelar</Button></div>
      </form> : <dl className="profile-preferences__summary">
        <div><dt>CPF</dt><dd>{profile.cpf}</dd></div><div><dt>WhatsApp</dt><dd>{profile.whatsappPhone}</dd></div>
        <div><dt>Endereço</dt><dd>{profile.address?.street}, {profile.address?.number}{profile.address?.complement ? ` — ${profile.address.complement}` : ''}</dd></div>
        <div><dt>Bairro e CEP</dt><dd>{profile.address?.neighborhood} — {profile.address?.postalCode}</dd></div>
        <div><dt>Cidade</dt><dd>{profile.city?.name}/{profile.city?.state}</dd></div>
      </dl>}
    </section>
  )
}

export default ProfileDataEditor
