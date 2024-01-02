package com.kmaebashi.dbutil;
import com.kmaebashi.dbutil.TableColumn;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ResultSetMapperTestDto {
    @TableColumn("TEST_KEY")
    public String testKey;

    @TableColumn("INT_VAL")
    public int intVal;

    @TableColumn("REAL_VAL")
    public double realVal;

    @TableColumn("BOOLEAN_VAL")
    public boolean booleanVal;

    @TableColumn("CHAR_VAL")
    public String charVal;

    @TableColumn("VARCHAR_VAL")
    public String varcharVal;

    @TableColumn("TEXT_VAL")
    public String textVal;

    @TableColumn("TIMESTAMP_VAL")
    public LocalDateTime timeStampVal;

    @TableColumn("DATE_VAL")
    public LocalDate dateVal;
}
