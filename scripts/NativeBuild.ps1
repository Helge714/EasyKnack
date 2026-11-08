# Shared compiler invocation for the developer build and Windows packaging.
function Invoke-EasyKnackNativeCompiler {
    param([string]$Cxx, [string]$SourceRoot, [string]$Generated,
          [string[]]$Sources, [string]$Output, [string[]]$ExtraArguments = @())
    $policy = @{}
    Get-Content -LiteralPath (Join-Path $SourceRoot 'build.properties') | ForEach-Object {
        if ($_ -match '^([^#=]+)=(.*)$') { $policy[$Matches[1]] = $Matches[2] }
    }
    if (!(Test-Path -LiteralPath (Join-Path $Generated 'ParameterContracts.hpp'))) {
        throw 'Missing Java/native parameter contracts. Run Maven process-classes before the native build.'
    }
    $arguments = @("-std=c++$($policy['cpp.standard'])") + ($policy['host.compile.flags'] -split ' ') +
        @('-I', "$SourceRoot/third_party", '-I', "$SourceRoot/src", '-I', $Generated) +
        $Sources + @('-o', $Output) + $ExtraArguments +
        @($policy['windows.libraries'] -split ' ' | ForEach-Object { "-l$_" })
    & $Cxx @arguments
    if ($LASTEXITCODE -ne 0) { throw "Native compilation failed: $Output" }
}
