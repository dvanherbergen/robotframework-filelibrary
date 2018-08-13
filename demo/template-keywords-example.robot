*** Setting ***
Library    FileLibrary

*** Variables ***
${RESOURCE_DIR}       ${CURDIR}/resources
${OUT_DIR}            ${CURDIR}/../target


*** Test Cases ***
Verify file can be created from template
  Comment    *messages

    Comment    "TODO..."
    Log    message    level    html    console    repr

hi there
        Verify SQL Result    sql    *values
        Generate File    templateFile    outputFile
        Verify XML Files Are Equal    file1    file2    *filters

New Test Cases
  Should Be True    5-1    OK


Do Something more
    Comment    TEST
    Ahoi

*** Keywords ***
Ahoi
  Log    ahoi
