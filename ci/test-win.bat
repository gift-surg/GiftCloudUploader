call mvn -U test -B
if not "%ERRORLEVEL%" == "0" (
    echo Exit Code = %ERRORLEVEL%
	exit /b 1
)