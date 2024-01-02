package com.kmaebashi.dbutil;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ResultSetMapperTestDto2 {
    @TableColumn("TEST_KEY")
    public String testKey;

    @TableColumn("INT_VAL")
    public Integer intVal;

    @TableColumn("REAL_VAL")
    public Double realVal;

    @TableColumn("BOOLEAN_VAL")
    public Boolean booleanVal;

    @TableColumn(value="CHAR_VAL", trim=true)
    public String charVal;

    @TableColumn("VARCHAR_VAL")
    public String varcharVal;

    @TableColumn("TEXT_VAL")
    public String textVal;

    @TableColumn("TIMESTAMP_VAL")
    public java.util.Date timeStampVal;

    @TableColumn("DATE_VAL")
    public java.util.Date dateVal;
}
