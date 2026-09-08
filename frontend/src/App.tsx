import { useEffect, useState } from 'react'
import { DataGsmLoginButton } from '@/component/DataGsmLoginButton'

type Student = { id: number; name: string; sex: string; grade: number; classNum: number; number: number; studentNumber: number; major: string; specialty: string | null; dormitoryFloor: number | null; dormitoryRoom: number | null; role: string; isLeaveSchool: boolean }
type UserInfo = { id: number; email: string; role: string; status: string; objectType: string | null; student: Student | null; teacher: unknown | null }

export default function App() {
  const [user, setUser] = useState<UserInfo | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const loadUser = async () => {
    setLoading(true); setError(null)
    try {
      const response = await fetch('/auth/me')
      if (response.status === 401) { setUser(null); return }
      if (!response.ok) throw new Error('학생 정보를 불러오지 못했습니다.')
      setUser(await response.json())
    } catch (e) { setError(e instanceof Error ? e.message : '알 수 없는 오류') }
    finally { setLoading(false) }
  }

  useEffect(() => { void loadUser() }, [])

  return <main>
    <section className="card">
      <p className="eyebrow">DATAGSM OAUTH</p>
      <h1>학생 정보 확인</h1>
      <p className="description">DataGSM 계정으로 로그인하고 내 학생 정보를 JSON으로 확인합니다.</p>
      {!user && <DataGsmLoginButton onClick={() => { window.location.href = '/auth/login' }} />}
      {user && <button className="button secondary" onClick={() => { window.location.href = '/auth/logout' }}>로그아웃</button>}
      {loading && <p>확인 중...</p>}
      {error && <p className="error">{error}</p>}
      {user && <pre>{JSON.stringify(user.student ?? user, null, 2)}</pre>}
    </section>
  </main>
}
