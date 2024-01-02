package com.kmaebashi.dbutil;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

class ResultSetMapperTest {
    private static Connection conn;
    @BeforeAll
    static void connectDb() throws Exception {
        ResourceBundle rb = ResourceBundle.getBundle("test");

        Class.forName(rb.getString("dbutiltest.driver-class-name"));
        conn = DriverManager.getConnection(rb.getString("dbutiltest.url"),
                rb.getString("dbutiltest.user-name"), rb.getString("dbutiltest.password"));
    }

    @AfterAll
    static void closeDb() throws Exception {
        conn.close();
    }

    @Test
    void toDtoTest001() throws Exception {
        String sql = """
                SELECT * FROM RESULTSETMAPPERTEST
                WHERE TEST_KEY = 'test01'
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        ResultSetMapperTestDto dto = ResultSetMapper.toDto(rs, ResultSetMapperTestDto.class);
        assertEquals("test01", dto.testKey);
        assertEquals(10, dto.intVal);
        assertEquals(10.5, dto.realVal);
        assertEquals(true, dto.booleanVal);
        assertEquals("abc       ", dto.charVal);
        assertEquals("varabc", dto.varcharVal);
        assertEquals("text", dto.textVal);
        assertEquals("2023-11-04 23:15:30", dto.timeStampVal.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        assertEquals("2023-11-05", dto.dateVal.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

    @Test
    void toDtoTest002() throws Exception {
        String sql = """
                SELECT * FROM RESULTSETMAPPERTEST
                WHERE TEST_KEY = 'test01'
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        ResultSetMapperTestDto2 dto = ResultSetMapper.toDto(rs, ResultSetMapperTestDto2.class);
        assertEquals("test01", dto.testKey);
        assertEquals(10, dto.intVal.intValue());
        assertEquals(10.5, dto.realVal.doubleValue());
        assertEquals(true, dto.booleanVal.booleanValue());
        assertEquals("abc", dto.charVal);
        assertEquals("varabc", dto.varcharVal);
        assertEquals("text", dto.textVal);
        assertEquals("2023-11-04 23:15:30", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(dto.timeStampVal));
        assertEquals("2023-11-05", new SimpleDateFormat("yyyy-MM-dd").format(dto.dateVal));
    }

    @Test
    void toDtoTest003() throws Exception {
        String sql = """
                SELECT * FROM RESULTSETMAPPERTEST
                WHERE TEST_KEY = 'test02'
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        ResultSetMapperTestDto dto = ResultSetMapper.toDto(rs, ResultSetMapperTestDto.class);
        assertEquals("test02", dto.testKey);
        assertEquals(0, dto.intVal);
        assertEquals(0.0, dto.realVal);
        assertEquals(false, dto.booleanVal);
        assertEquals(null, dto.charVal);
        assertEquals(null, dto.varcharVal);
        assertEquals(null, dto.textVal);
        assertEquals(null, dto.timeStampVal);
        assertEquals(null, dto.dateVal);
    }

    @Test
    void toDtoTest004() throws Exception {
        String sql = """
                SELECT * FROM RESULTSETMAPPERTEST
                WHERE TEST_KEY = 'test02'
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        ResultSetMapperTestDto2 dto = ResultSetMapper.toDto(rs, ResultSetMapperTestDto2.class);
        assertEquals("test02", dto.testKey);
        assertEquals(null, dto.intVal);
        assertEquals(null, dto.realVal);
        assertEquals(null, dto.booleanVal);
        assertEquals(null, dto.charVal);
        assertEquals(null, dto.varcharVal);
        assertEquals(null, dto.textVal);
        assertEquals(null, dto.timeStampVal);
        assertEquals(null, dto.dateVal);
    }

    @Test
    void toDtoTest005() throws Exception {
        String sql = """
                SELECT * FROM RESULTSETMAPPERTEST
                WHERE TEST_KEY = 'testXX'
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        ResultSetMapperTestDto dto = ResultSetMapper.toDto(rs, ResultSetMapperTestDto.class);
        assertNull(dto);
    }

    @Test
    void toDtoTestError001() throws Exception {
        String sql = """
                SELECT * FROM RESULTSETMAPPERTEST
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        try {
            ResultSetMapperTestDto dto = ResultSetMapper.toDto(rs, ResultSetMapperTestDto.class);
        } catch (Exception ex) {
            assertTrue(ex instanceof MultipleMatchException);
            assertEquals("2件検索されました。", ex.getMessage());
            return;
        }
        fail();
    }

    @Test
    void toDtoTestError002() throws Exception {
        String sql = """
                SELECT INT_VAL FROM RESULTSETMAPPERTEST
                WHERE TEST_KEY = 'test01'
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        try {
            ResultSetMapperErrorDto1 dto = ResultSetMapper.toDto(rs, ResultSetMapperErrorDto1.class);
        } catch (Exception ex) {
            assertTrue(ex instanceof UnsupportedTypeException);
            assertEquals("整数を型doubleに変換できません(列:INT_VAL)。", ex.getMessage());
            return;
        }
        fail();
    }

    @Test
    void toDtoTestError003() throws Exception {
        String sql = """
                SELECT REAL_VAL FROM RESULTSETMAPPERTEST
                WHERE TEST_KEY = 'test01'
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        try {
            ResultSetMapperErrorDto1 dto = ResultSetMapper.toDto(rs, ResultSetMapperErrorDto1.class);
        } catch (Exception ex) {
            assertTrue(ex instanceof UnsupportedTypeException);
            assertEquals("実数を型intに変換できません(列:REAL_VAL)。", ex.getMessage());
            return;
        }
        fail();
    }

    @Test
    void toDtoTestError004() throws Exception {
        String sql = """
                SELECT BOOLEAN_VAL FROM RESULTSETMAPPERTEST
                WHERE TEST_KEY = 'test01'
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        try {
            ResultSetMapperErrorDto1 dto = ResultSetMapper.toDto(rs, ResultSetMapperErrorDto1.class);
        } catch (Exception ex) {
            assertTrue(ex instanceof UnsupportedTypeException);
            assertEquals("ブーリアンを型intに変換できません(列:BOOLEAN_VAL)。", ex.getMessage());
            return;
        }
        fail();
    }

    @Test
    void toDtoTestError005() throws Exception {
        String sql = """
                SELECT DATE_VAL FROM RESULTSETMAPPERTEST
                WHERE TEST_KEY = 'test01'
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        try {
            ResultSetMapperErrorDto1 dto = ResultSetMapper.toDto(rs, ResultSetMapperErrorDto1.class);
        } catch (Exception ex) {
            assertTrue(ex instanceof UnsupportedTypeException);
            assertEquals("DATE型を型intに変換できません(列:DATE_VAL)。", ex.getMessage());
            return;
        }
        fail();
    }

    @Test
    void toDtoTestError006() throws Exception {
        String sql = """
                SELECT TIMESTAMP_VAL FROM RESULTSETMAPPERTEST
                WHERE TEST_KEY = 'test01'
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        try {
            ResultSetMapperErrorDto1 dto = ResultSetMapper.toDto(rs, ResultSetMapperErrorDto1.class);
        } catch (Exception ex) {
            assertTrue(ex instanceof UnsupportedTypeException);
            assertEquals("TIMESTAMP型を型intに変換できません(列:TIMESTAMP_VAL)。", ex.getMessage());
            return;
        }
        fail();
    }

    @Test
    void toDtoTestError007() throws Exception {
        String sql = """
                SELECT CHAR_VAL FROM RESULTSETMAPPERTEST
                WHERE TEST_KEY = 'test01'
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        ResultSetMapperErrorDto1 dto = ResultSetMapper.toDto(rs, ResultSetMapperErrorDto1.class);
    }

    @Test
    void toDtoTestError008() throws Exception {
        String sql = """
                SELECT NUMERIC_VAL FROM RESULTSETMAPPERTEST2
                WHERE TEST_KEY = 'test01'
                """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        try {
            ResultSetMapperErrorDto2 dto = ResultSetMapper.toDto(rs, ResultSetMapperErrorDto2.class);
        } catch (Exception ex) {
            assertTrue(ex instanceof UnsupportedTypeException);
            assertEquals("java.sql.Typesの2は未対応です。", ex.getMessage());
            return;
        }
        fail();
    }
}