import { useEffect, useState } from 'react'
import { DataGsmLoginButton } from '@/component/DataGsmLoginButton'

type UserInfo = {
  student?: unknown
}

export default function App() {
  const [student, setStudent] = useState<unknown>(null)

  useEffect(() => {
    const loadStudent = async () => {
      try {
        const response = await fetch('/auth/me', { credentials: 'include' })

        if (!response.ok) {
          return
        }

        const user = (await response.json()) as UserInfo
        if (user.student !== undefined && user.student !== null) {
          setStudent(user.student)
        }
      } catch {
        // A missing session leaves the login button as the only content.
      }
    }

    void loadStudent()
  }, [])

  if (student !== null) {
    return (
      <main className="student-page">
        <pre className="student-json">{JSON.stringify(student, null, 2)}</pre>
      </main>
    )
  }

  return (
    <main className="login-page">
      <DataGsmLoginButton onClick={() => { window.location.href = '/auth/login' }} />
    </main>
  )
}
