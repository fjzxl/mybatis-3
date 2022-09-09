package com.example.demo.dao;

public class Student {
  private Integer id;
  private String studentID;
  private String name;
  private String password;
  private Integer age;
  private Integer sex;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getStudentID() {
    return studentID;
  }

  public void setStudentID(String studentID) {
    this.studentID = studentID;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Integer getAge() {
    return age;
  }

  public void setAge(Integer age) {
    this.age = age;
  }

  public Integer getSex() {
    return sex;
  }

  public void setSex(Integer sex) {
    this.sex = sex;
  }

  @Override
  public String toString() {
    return "User{" +
      "id=" + id +
      ", studentID='" + studentID + '\'' +
      ", name='" + name + '\'' +
      ", password='" + password + '\'' +
      ", age=" + age +
      ", sex=" + sex +
      '}';
  }
}
