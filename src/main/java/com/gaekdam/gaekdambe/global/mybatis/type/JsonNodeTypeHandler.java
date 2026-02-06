package com.gaekdam.gaekdambe.global.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonNodeTypeHandler extends BaseTypeHandler<JsonNode> {
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, JsonNode parameter, JdbcType jdbcType) throws SQLException {
    ps.setString(i, parameter == null ? null : parameter.toString());
  }

  @Override
  public JsonNode getNullableResult(ResultSet rs, String columnName) throws SQLException {
    String json = rs.getString(columnName);
    try { return json == null ? null : OBJECT_MAPPER.readTree(json); }
    catch (Exception e) { throw new SQLException("Failed to parse JSON column '" + columnName + "'", e); }
  }

  @Override
  public JsonNode getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    String json = rs.getString(columnIndex);
    try { return json == null ? null : OBJECT_MAPPER.readTree(json); }
    catch (Exception e) { throw new SQLException("Failed to parse JSON at index " + columnIndex, e); }
  }

  @Override
  public JsonNode getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    String json = cs.getString(columnIndex);
    try { return json == null ? null : OBJECT_MAPPER.readTree(json); }
    catch (Exception e) { throw new SQLException("Failed to parse JSON from CallableStatement at index " + columnIndex, e); }
  }
}