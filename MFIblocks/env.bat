@echo off
IF EXIST C:\Apache-maven-3.1.1 (
@echo Apache-maven-3.1.1 exists under C drive.
@set M2_HOME=C:\Apache-maven-3.1.1
goto addMvnToPath
) 
IF EXIST C:\worksapce\Apache-maven-3.1.1 (
@echo Apache-maven-3.1.1 exists under C:\workspace drive.
@set M2_HOME=C:\worksapce\Apache-maven-3.1.1
goto addMvnToPath
)

echo.
echo ERROR: Apache-maven not found in your environment.
echo Please add Apache-maven to your ststem and place it either 
echo at "C:" or "C:\worksapce"
echo.

:addMvnToPath

@set PATH=%PATH%;%M2_HOME%\bin;
@echo Apache Maven is ready for compilation!