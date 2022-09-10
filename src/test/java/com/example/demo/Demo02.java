package com.example.demo;

import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.junit.jupiter.api.Test;

public class Demo02 {
  @Test
  void test(){
    PropertyTokenizer propertyTokenizer = new PropertyTokenizer("user[id].name");
    System.out.println(propertyTokenizer);
  }
}
