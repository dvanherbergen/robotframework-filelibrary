*** Setting ***

Library    FileLibrary    WITH NAME    db1
Library    FileLibrary    WITH NAME    db2

Suite Setup   Initialize database
Suite Teardown    Stop All

*** Variables ***
${RESOURCE_DIR}       ${CURDIR}/resources


*** Test Cases ***
Load data from xls
    Comment    Insert data from XLS
    db1.Refresh Tables    ${RESOURCE_DIR}/db_data.xlsx    MYSCHEMA
    db1.Verify SQL Result    select email from OWNERS where name = 'jeff'    jeff@hotmail.com
    db2.Verify SQL Result    select email from OWNERS where name = 'jeff'    jeff@hotmail.com
    db1.Verify SQL Result    select count(*) from PETS where name = 'Twinkie'        1
    db2.Verify SQL Result    select count(*) from PETS where name = 'Twinkie'    0
    
*** Keywords ***
Initialize database
    Set Log Level    TRACE
    Comment    Setting up database
    db1.getRandomUUID
    db2.getRandomUUID
    db1.Connect    jdbc:hsqldb:mem:mymemdb1    ${empty}    ${empty}
    db2.Connect    jdbc:hsqldb:mem:mymemdb2    ${empty}    ${empty}
    Comment    Build tables from DDL
    db1.Execute SQL    ${RESOURCE_DIR}/db_create.sql
    Comment    Build tables from DDL
    db2.Execute SQL    ${RESOURCE_DIR}/db_create.sql
    Comment    Insert data using SQL
    db1.Execute SQL    ${RESOURCE_DIR}/db_inserts.sql

    
Stop All
    db1.stop
    db2.stop

