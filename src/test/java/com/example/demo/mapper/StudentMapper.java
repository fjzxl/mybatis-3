package com.example.demo.mapper;

import com.example.demo.dao.Student;
import org.apache.ibatis.annotations.Param;

public interface StudentMapper {
  Student selectStudent(Integer id);

  Student selectStudentByUserName(@Param("username") String userName);
}
