param(
    [switch]$DemoPayment
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$jar = Join-Path $projectRoot 'pet-server\target\pet-server-1.0.0-SNAPSHOT.jar'
$java = Join-Path $env:JAVA_HOME 'bin\java.exe'
if (-not (Test-Path -LiteralPath $jar)) { throw "Backend JAR not found: $jar. Run 'mvn package' in pet-server first." }
if (-not (Test-Path -LiteralPath $java)) { throw "Java 17 not found: $java. Set JAVA_HOME to a Java 17 installation." }

$securePassword = Read-Host 'Enter the local MySQL root password' -AsSecureString
$passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
try {
    $env:DB_URL = 'jdbc:mysql://127.0.0.1:3306/pet_boarding?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai'
    $env:DB_USERNAME = 'root'
    $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
    $env:JWT_ISSUER = 'http://127.0.0.1:8080'
    $random = [Security.Cryptography.RandomNumberGenerator]::Create()
    try {
        $keyBytes = New-Object byte[] 32
        $random.GetBytes($keyBytes)
        $env:JWT_SECRET_BASE64 = [Convert]::ToBase64String($keyBytes)
    } finally { $random.Dispose() }

    # Public pages work without WeChat credentials; login needs real matching values.
    if (-not $env:WECHAT_APP_ID) { $env:WECHAT_APP_ID = 'local-preview-only' }
    if (-not $env:WECHAT_APP_SECRET) { $env:WECHAT_APP_SECRET = 'local-preview-only' }
    $env:PAYMENT_DEMO_ENABLED = if ($DemoPayment) { 'true' } else { 'false' }

    Write-Host 'Starting pet-server at http://127.0.0.1:8080 ...'
    Write-Host 'Keep this window open while using the mini program. Press Ctrl+C to stop.'
    & $java -jar $jar
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
    Remove-Item Env:DB_PASSWORD, Env:JWT_SECRET_BASE64 -ErrorAction SilentlyContinue
}
