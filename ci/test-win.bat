call mvn install -B -P Applet,Application,Webstart
if not "%ERRORLEVEL%" == "0" (
    echo Exit Code = %ERRORLEVEL%
	exit /b 1
)