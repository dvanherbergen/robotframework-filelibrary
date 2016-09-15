# robotframework-filelibrary
RobotFramework library for generating data files.


## Usage
- add jdbc driver jar + filelibrary jar to PYTHONPATH env or specify jdbcdriver as argument for filelibrary

*** Settings ***
Library           FileLibrary
Library           FileLibrary    ${jdbcDriverPath}

*** Test Cases ***
Test SQL Verification
    Verify SQL Result    select 1,2,3 from dual    1    2    3


## TODO
? support null parameter in where clause
? load datafiles into DB using DBUnit
