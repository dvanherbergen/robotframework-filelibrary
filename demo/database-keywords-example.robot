*** Setting ***
Library    FileLibrary

Suite Setup   Initialize database
Suite Teardown    Disconnect

*** Variables ***
${RESOURCE_DIR}       ${CURDIR}/resources


*** Test Cases ***
Load data from xls
    Comment    Insert data from XLS
    Replace Tables    ${RESOURCE_DIR}/db_data.xlsx
    Verify SQL Result    select email from OWNERS where name = 'jeff'    jeff@hotmail.com
    Verify SQL Result    select 1 from PETS where name = 'twinkie'        1


*** Keywords ***
Initialize database
    Comment    Setting up database
    Connect    com.mysql.cj.jdbc.Driver    jdbc:mysql://localhost/demo    user    secret2
    Comment    Build tables from DDL
    Execute SQL    ${RESOURCE_DIR}/db_create.sql
    Comment    Insert data using SQL
    Execute SQL    ${RESOURCE_DIR}/db_inserts.sql

