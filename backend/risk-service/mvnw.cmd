@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    https://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM
@REM Required ENV vars:
@REM   JAVA_HOME - location of a JDK home dir
@REM
@REM Optional ENV vars
@REM   MAVEN_OPTS - parameters passed to the Java VM when running Maven
@REM   MAVEN_SKIP_RC - flag to disable loading of mavenrc files
@REM ----------------------------------------------------------------------------

@IF "%OS%"=="Windows_NT" @setlocal
@IF "%OS%"=="Windows_NT" @set JAVA_EXE=%JAVA_HOME%/bin/java.exe
@IF NOT "%OS%"=="Windows_NT" @set JAVA_EXE=%JAVA_HOME%\bin\java.exe

@set ERROR_CODE=0

@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set MAVEN_CMD_LINE_ARGS=%*

@REM Find the project base directory; i.e. the directory that contains the folder ".mvn".
@REM Fallback to current directory if not found.

set MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%
IF NOT "%MAVEN_PROJECTBASEDIR%"=="" goto endDetectBaseDir

set EXEC_DIR=%CD%
set WDIR=%EXEC_DIR%
:findBaseDir
IF EXIST "%WDIR%"\.mvn goto baseDirFound
cd ..
IF "%WDIR%"=="%CD%" goto baseDirNotFound
set WDIR=%CD%
goto findBaseDir

:baseDirFound
set MAVEN_PROJECTBASEDIR=%WDIR%
cd "%EXEC_DIR%"
goto endDetectBaseDir

:baseDirNotFound
set MAVEN_PROJECTBASEDIR=%EXEC_DIR%
cd "%EXEC_DIR%"

:endDetectBaseDir

IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar" (
    if not "%MVNW_REPOURL%" == "" (
        SET MVNW_DOWNLOAD_URL=%MVNW_REPOURL%/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar
    ) else (
        SET MVNW_DOWNLOAD_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/maven-wrapper-3.3.2.jar
    )
    SET MVNW_JAR_PATH=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar
    @echo Downloading from: %MVNW_DOWNLOAD_URL%
    powershell -Command "&{"^
        "$webclient = new-object System.Net.WebClient;"^
        "if (-not ([string]::IsNullOrEmpty('%MVNW_USERNAME%') -and [string]::IsNullOrEmpty('%MVNW_PASSWORD%'))) {"^
        "$webclient.Credentials = new-object System.Net.NetworkCredential('%MVNW_USERNAME%', '%MVNW_PASSWORD%');"^
        "}"^
        "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; $webclient.DownloadFile('%MVNW_DOWNLOAD_URL%', '%MVNW_JAR_PATH%')"^
        "}"
    if "%ERRORLEVEL%"=="0" goto continueWithReactorBuild
    echo WARNING: Failed to download maven-wrapper.jar from %MVNW_DOWNLOAD_URL%
    goto continueWithReactorBuild
)
:continueWithReactorBuild

@REM Detect JAVA_HOME
IF NOT "%JAVA_HOME%"=="" goto javaHomeFound
set JAVA_HOME=
for %%j in (java.exe) do (
    for /f "delims=" %%i in ('where %%j 2^>nul') do (
        set "JAVA_HOME=%%~dpi.."
        goto javaHomeFound
    )
)

:javaHomeFound
IF NOT EXIST "%JAVA_HOME%\bin\java.exe" (
    echo.
    echo Error: JAVA_HOME is set to an invalid directory.
    echo JAVA_HOME = "%JAVA_HOME%"
    echo Please set the JAVA_HOME variable in your environment to match the location of your Java installation.
    echo.
    goto error
)

set JAVA_EXE=%JAVA_HOME%\bin\java.exe

@REM Collect all arguments for the java command
set MAVEN_JAVA_EXE="%JAVA_EXE%"
set WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain
set WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set WRAPPER_PROPERTIES="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties"

set DOWNLOAD_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip

%MAVEN_JAVA_EXE% %MAVEN_OPTS% ^
    -classpath %WRAPPER_JAR% ^
    "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
    "-Dmaven.wrapper.jar=%WRAPPER_JAR%" ^
    "-Dmaven.wrapper.properties=%WRAPPER_PROPERTIES%" ^
    "-Dmaven.wrapper.download.url=%DOWNLOAD_URL%" ^
    %WRAPPER_LAUNCHER% %MAVEN_CMD_LINE_ARGS%
if ERRORLEVEL 1 goto error
goto end

:error
set ERROR_CODE=1

:end
@IF "%OS%"=="Windows_NT" @endlocal
@cmd /C exit /B %ERROR_CODE%
