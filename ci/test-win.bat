call mvn -e -U clean test -B -P Webstart
if not "%ERRORLEVEL%" == "0" (
    echo Exit Code = %ERRORLEVEL%
	exit /b 1
)