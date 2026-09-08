# DataGSM OAuth Flow

React + TypeScript 프론트엔드와 Kotlin/Spring Boot BFF 백엔드 예제입니다.

## 준비

Java 21, Node.js 20 이상과 DataGSM OAuth Client ID가 필요합니다.
callback URI는 `http://localhost:5173/auth/callback`으로 DataGSM에 등록하세요.

루트에서 `.env.example`을 복사해 `.env`를 만든 뒤 Client ID를 입력합니다.

```powershell
Copy-Item .env.example .env
```

예시는 [`.env.example`](.env.example)에 있습니다.

## 실행

```powershell
.\start.ps1
```

## 참고 사항

브라우저는 <http://localhost:5173>만 사용합니다. Vite가 `/auth`와 `/api`를 Kotlin 서버(`8080`)로 reverse proxy합니다.

Kotlin 서버는 PKCE verifier/state와 Access Token을 세션에 보관하고, `/auth/me`에서 DataGSM `/userinfo`를 호출합니다. React는 반환된 `student` 객체를 JSON으로 출력합니다.
