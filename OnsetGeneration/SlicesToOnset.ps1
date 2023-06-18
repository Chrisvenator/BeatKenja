$folderPath = "C:\Program Files (x86)\Steam\steamapps\common\Beat Saber\_SongsToTimings\SlicesToOnset\slices"

# Überprüfen, ob der angegebene Ordner existiert
if (-not (Test-Path -Path $folderPath)) {
    Write-Host "Der angegebene Ordner existiert nicht."
    exit
}

# Alle .wav-Dateien im Ordner auflisten
$files = Get-ChildItem -Path $folderPath -Filter "*.wav"

$onsets = "";
$onsetLength = 0;
# Über jede Datei iterieren und die Länge ausgeben
foreach ($file in $files) {
    $duration = Get-WaveFileDuration -Path $file.FullName
    Write-Host "Datei: $($file.Name)"
    Write-Host "Länge: $($duration) Sekunden"
    Write-Host "-----"

    $onsetLength += $duration;
    $onsets += $onsetLength.ToString() + "`r`n"
}

$onsets = $onsets | ForEach-Object { $_ -replace ",", "." }
Set-Content -Path "C:\Program Files (x86)\Steam\steamapps\common\Beat Saber\_SongsToTimings\SlicesToOnset\onsets.txt" -Value $onsets

# Funktion zum Abrufen der Länge einer .wav-Datei mit ffprobe
function Get-WaveFileDuration {
    param (
        [Parameter(Mandatory = $true, ValueFromPipeline = $true, Position = 0)]
        [Alias("FullName")]
        [string]$Path
    )

    process {
        $duration = 0



        $command = "ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 `"$Path`""
        $output = Invoke-Expression -Command $command

        if ($output -match '\d+(\.\d+)?') {
            $duration = [double]$Matches[0]
        }

        # ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 "C:\Program Files (x86)\Steam\steamapps\common\Beat Saber\_SongsToTimings\SlicesToOnset\slices\YASOBI - Idol_0.wav"

        return $duration
    }
}