import DBlack from '@/asset/svg/DBlack'
import DWhite from '@/asset/svg/DWhite'

interface DataGsmLoginButtonProps {
  className?: string
  disabled?: boolean
  onClick?: () => void
}

export function DataGsmLoginButton({ className, disabled = false, onClick }: DataGsmLoginButtonProps) {
  return (
    <button
      type="button"
      className={`data-gsm-login-button ${className ?? ''}`}
      disabled={disabled}
      onClick={onClick}
    >
      <span className="icon-light"><DWhite /></span>
      <span className="icon-dark"><DBlack /></span>
      DataGSM &#xC73C;&#xB85C; &#xB85C;&#xADF8;&#xC778;
    </button>
  )
}
