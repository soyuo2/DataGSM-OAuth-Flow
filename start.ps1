# .env의 값을 현재 프로세스 환경변수로 올린 뒤 Kotlin BFF와 React를 시작합니다.
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$envFile = Join-Path $projectRoot '.env'

if (-not (Test-Path -LiteralPath $envFile)) {
  Write-Error ".env 파일이 없습니다. .env.example을 복사해 .env를 만든 뒤 값을 입력하세요."
  exit 1
}

Get-Content -LiteralPath $envFile | ForEach-Object {
  $line = $_.Trim()
  if ($line -and -not $line.StartsWith('#')) {
    $parts = $line -split '=', 2
    if ($parts.Count -eq 2) {
      $name = $parts[0].Trim()
      $value = $parts[1].Trim().Trim('"').Trim("'")
      [Environment]::SetEnvironmentVariable($name, $value, 'Process')
    }
  }
}

Start-Process powershell -ArgumentList '-NoExit', '-Command', "Set-Location '$projectRoot'; .\gradlew.bat bootRun" -WorkingDirectory $projectRoot
Start-Process powershell -ArgumentList '-NoExit', '-Command', "Set-Location '$projectRoot\frontend'; npm install; npm run dev" -WorkingDirectory (Join-Path $projectRoot 'frontend')
Write-Host 'Frontend: http://localhost:5173'
Write-Host 'Kotlin BFF: http://localhost:8080'
