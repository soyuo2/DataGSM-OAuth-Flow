# .env의 값을 현재 프로세스 환경변수로 올린 뒤 Kotlin BFF와 React를
# 별도 창 없이 하나의 터미널에서 실행하고 로그를 함께 출력합니다.
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

$backendJob = Start-Job -Name 'datagsm-backend' -ScriptBlock {
  Set-Location $using:projectRoot
  & .\gradlew.bat bootRun 2>&1
}

$frontendJob = Start-Job -Name 'datagsm-frontend' -ScriptBlock {
  Set-Location (Join-Path $using:projectRoot 'frontend')
  npm install 2>&1
  npm run dev 2>&1
}

Write-Host 'Frontend: http://localhost:5173'
Write-Host 'Kotlin BFF: http://localhost:8080'
Write-Host '두 서버를 하나의 터미널에서 실행 중입니다. 종료하려면 Ctrl+C를 누르세요.'

try {
  while ($backendJob.State -in @('Running', 'NotStarted') -or $frontendJob.State -in @('Running', 'NotStarted')) {
    Receive-Job -Id $backendJob.Id | ForEach-Object { Write-Host '[BE] ' $_ }
    Receive-Job -Id $frontendJob.Id | ForEach-Object { Write-Host '[FE] ' $_ }
    Start-Sleep -Milliseconds 150
  }
}
finally {
  Stop-Job -Id $backendJob.Id, $frontendJob.Id -ErrorAction SilentlyContinue
  Remove-Job -Id $backendJob.Id, $frontendJob.Id -Force -ErrorAction SilentlyContinue
}
