# Load .env and run Kotlin BFF and React in one terminal.
$OutputEncoding = [System.Text.UTF8Encoding]::new()
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()
$null = chcp 65001

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$envFile = Join-Path $projectRoot '.env'
if (-not (Test-Path -LiteralPath $envFile)) { Write-Error ".env was not found."; exit 1 }

Get-Content -LiteralPath $envFile | ForEach-Object {
  $line = $_.Trim()
  if ($line -and -not $line.StartsWith('#')) {
    $parts = $line -split '=', 2
    if ($parts.Count -eq 2) {
      [Environment]::SetEnvironmentVariable($parts[0].Trim(), $parts[1].Trim().Trim('"').Trim("'"), 'Process')
    }
  }
}

if ($env:DATAGSM_JAVA_HOME -and (Test-Path -LiteralPath (Join-Path $env:DATAGSM_JAVA_HOME 'bin\java.exe'))) {
  $env:JAVA_HOME = $env:DATAGSM_JAVA_HOME
  $env:Path = "$(Join-Path $env:JAVA_HOME 'bin');$env:Path"
  Write-Host "Java: $env:JAVA_HOME"
} else { Write-Host 'Java: using the existing JAVA_HOME or PATH configuration' }

$backendJob = Start-Job -Name 'datagsm-backend' -ScriptBlock { Set-Location $using:projectRoot; & .\gradlew.bat bootRun 2>&1 }
$frontendJob = Start-Job -Name 'datagsm-frontend' -ScriptBlock { Set-Location (Join-Path $using:projectRoot 'frontend'); npm install 2>&1; npm run dev 2>&1 }

Write-Host 'Frontend: http://localhost:5173'
Write-Host 'Kotlin BFF: http://localhost:8080'
Write-Host 'Both servers are running in this terminal. Press Ctrl+C to stop.'
try {
  while ($backendJob.State -in @('Running', 'NotStarted') -or $frontendJob.State -in @('Running', 'NotStarted')) {
    Receive-Job -Id $backendJob.Id | ForEach-Object { Write-Host '[BE] ' $_ }
    Receive-Job -Id $frontendJob.Id | ForEach-Object { Write-Host '[FE] ' $_ }
    Start-Sleep -Milliseconds 150
  }
} finally {
  Stop-Job -Id $backendJob.Id, $frontendJob.Id -ErrorAction SilentlyContinue
  Remove-Job -Id $backendJob.Id, $frontendJob.Id -Force -ErrorAction SilentlyContinue
}
