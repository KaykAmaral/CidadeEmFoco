function Brand({ compact = false, inverse = false }) {
  const classNames = [
    'brand',
    compact && 'brand--compact',
    inverse && 'brand--inverse',
  ]
    .filter(Boolean)
    .join(' ')

  return (
    <div className={classNames} aria-label="Cidade em Foco">
      <img
        className="brand__icon"
        src="/brand/cidade-em-foco-app-icon.png"
        alt=""
      />

      {!compact && (
        <div className="brand__text">
          <strong>Cidade em Foco</strong>
          <span>Nossa cidade, nosso olhar.</span>
        </div>
      )}
    </div>
  )
}

export default Brand
