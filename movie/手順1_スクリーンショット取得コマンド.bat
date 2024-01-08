@echo off
setlocal enabledelayedexpansion
pause
set "ffmpeg=C:\pleiades\2023-06\workspace\movie\ffmpeg.exe"
set "inputDir=C:\pleiades\2023-06\workspace\movie\input_counter"
set "outputDir=C:\pleiades\2023-06\workspace\movie\screenshot"
for %%F in ("%inputDir%\*.*") do (
     set "inputFile=%%~dpnF%%~xF"
     set "outputFile=!outputDir!\%%~nF.png"
     "%ffmpeg%" -ss 00:00:06 -i "!inputFile!" -vframes 1 -q:v 2 "!outputFile!"
     echo Created screenshot: "!outputFile!"
)
echo All screenshots created.