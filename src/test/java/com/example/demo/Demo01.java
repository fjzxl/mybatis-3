package com.example.demo;

import com.example.demo.dao.Student;
import com.example.demo.mapper.StudentMapper;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;

public class Demo01 {

  @Test
  void test01() {
    try {
      String resource = "resources/mybatis-config.xml";
      InputStream inputStream = Resources.getResourceAsStream(resource);
      SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(inputStream);

      try (SqlSession session = sessionFactory.openSession()) {
        /**
         * 这SqlSession是DefaultSqlSession
         */
//        Student student01 = session.selectOne("com.example.demo.mapper.StudentMapper.selectStudent", 1);
//        System.out.println(student01);

        StudentMapper studentMapper = session.getMapper(StudentMapper.class);
        Student student02 = studentMapper.selectStudent(2);
        System.out.println(student02);

        student02 = studentMapper.selectStudentByUserName("李四");
        System.out.println(student02);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  void jdbcTest() {
    InputStream is = null;
    Connection conn = null;
    String DRIVER_NAME = null;
    String USER = null;
    String PASSWORD = null;
    String SQL_URL = null;
    String resource = "resources/jdbc.properties";
    try {
      is = ClassLoader.getSystemResourceAsStream(resource);
      Properties properties = new Properties();
      properties.load(is);
      DRIVER_NAME = properties.getProperty("driver");
      USER = properties.getProperty("username");
      PASSWORD = properties.getProperty("password");
      SQL_URL = properties.getProperty("url");
      Class.forName(DRIVER_NAME);
      conn = DriverManager.getConnection(SQL_URL, USER, PASSWORD);
      System.out.println(conn.getMetaData().getDatabaseProductName());

      PreparedStatement preparedStatement = conn.prepareStatement("SELECT  * FROM student where id=?");
      preparedStatement.setString(1, "1");
      ResultSet resultSet = preparedStatement.executeQuery();
      ResultSetMetaData metaData = resultSet.getMetaData();
      int columnCount = metaData.getColumnCount();
      System.out.println(columnCount);
      List list = new ArrayList<Map>();
      if (resultSet.next()) {
        Map<String, Object> jsonMap = new HashMap<>();
        for (int i = 1; i <= columnCount; i++) {
          String columnTypeName = metaData.getColumnTypeName(i);
          String columnName = metaData.getColumnName(i);
          if ("INT".equals(columnTypeName)) {
            int anInt = resultSet.getInt(columnName);
            jsonMap.put(columnName, anInt);
          } else {
            String s = resultSet.getString(columnName);
            jsonMap.put(columnName, s);
          }
        }
        list.add(jsonMap);
      }
      resultSet.close();
      preparedStatement.close();
      System.out.println(list);
    } catch (IOException e) {
      e.printStackTrace();
    } catch (SQLException throwables) {
      throwables.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } finally {
      try {
        if (conn != null)
          conn.close();
      } catch (SQLException throwables) {
        throwables.printStackTrace();
      }
      try {
        if (is != null) {
          is.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Test
  void parserTest(){
    String resource = "resources/test-config.xml";
    try (InputStream inputStream = Resources.getResourceAsStream(resource)) {
      XPathParser parser = new XPathParser(inputStream, false, null, new XMLMapperEntityResolver());
      XNode xNode = parser.evalNode("/configuration");

      XNode properties = xNode.evalNode("properties");
      Properties defaults = properties.getChildrenAsProperties();
      String propertiesResource = properties.getStringAttribute("resource");
      defaults.putAll(Resources.getResourceAsProperties(propertiesResource));
      System.out.println(defaults);
      parser.setVariables(defaults);
      System.out.println(xNode.evalNode("environments"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    try(InputStream inputStream = Resources.getResourceAsStream(resource)){
      XPathParser parserWithValidation = new XPathParser(inputStream, true, null, new XMLMapperEntityResolver());
      XNode xNodeValidation = parserWithValidation.evalNode("/configuration");
      XNode propertiesValidation = xNodeValidation.evalNode("properties");
      Properties defaultsValidation = propertiesValidation.getChildrenAsProperties();
      String propertiesResourceValidation = propertiesValidation.getStringAttribute("resource");
      defaultsValidation.putAll(Resources.getResourceAsProperties(propertiesResourceValidation));
      System.out.println(defaultsValidation);
      parserWithValidation.setVariables(defaultsValidation);
      System.out.println(xNodeValidation.evalNode("environments"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
