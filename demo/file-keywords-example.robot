*** Setting ***
Library    OperatingSystem
Library    FileLibrary

*** Variables ***
${RESOURCE_DIR}       ${CURDIR}/resources
${OUT_DIR}            ${CURDIR}/../target/test_output


*** Test Cases ***
Verify 2 files can be concatenated and the result compared
    Create Directory    ${OUT_DIR}
    Empty Directory    ${OUT_DIR}
    Concatenate Files   ${OUT_DIR}/result_file.txt   ${RESOURCE_DIR}/file_1.txt  ${RESOURCE_DIR}/file_2.txt
    Verify files are equal  ${RESOURCE_DIR}/file_3.txt  ${OUT_DIR}/result_file.txt
    Comment    "Archiving results..."
    Zip Files    ${OUT_DIR}/result.zip    ${OUT_DIR}/result_file.txt    ${RESOURCE_DIR}/file_3.txt


Verify XSLT transformation can be applied
    Empty Directory    ${OUT_DIR}
    Apply XSLT    ${RESOURCE_DIR}/article.xml    ${RESOURCE_DIR}/article.xslt    ${OUT_DIR}/article_result.xml
    Comment    Verify the generated file matches our expected result, and ignore 2 nodes (1 using {IGNORE} and 1 using xpath filter)
    Verify XML files are equal    ${OUT_DIR}/article_result.xml    ${RESOURCE_DIR}/authors.xml    /Authors/GeneratedBy