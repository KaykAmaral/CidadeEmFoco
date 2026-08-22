function Button({ children, variant = 'primary', className = '', ...props }) {
  const classNames = `button button--${variant} ${className}`.trim()

  return (
    <button className={classNames} type="button" {...props}>
      {children}
    </button>
  )
}

export default Button
