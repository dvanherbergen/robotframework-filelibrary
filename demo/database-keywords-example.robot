*** Setting ***
Library    FileLibrary
Library     String

Suite Setup   Initialize database
Suite Teardown    Disconnect

*** Variables ***
${RESOURCE_DIR}       ${CURDIR}/resources


*** Test Cases ***
Load data from xls
    Comment    Insert data from XLS
    Refresh Tables    ${RESOURCE_DIR}/db_data.xlsx    MYSCHEMA
    Verify SQL Result    select email from OWNERS where name = 'jeff'    jeff@hotmail.com
    Verify SQL Result    select 1 from PETS where name = 'Twinkie'        1

*** Keywords ***
Initialize database
    Comment    Setting up database
    Connect    jdbc:hsqldb:mem:mymemdb    ${empty}    ${empty}
    Comment    Build tables from DDL
    Execute SQL    ${RESOURCE_DIR}/db_create.sql
    Comment    Insert data using SQL
    Execute SQL    ${RESOURCE_DIR}/db_inserts.sql

