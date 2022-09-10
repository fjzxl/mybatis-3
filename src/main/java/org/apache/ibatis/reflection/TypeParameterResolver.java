/**
 *    Copyright 2009-2019 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.reflection;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

/**
 * @author Iwao AVE!
 */
public class TypeParameterResolver {

  /**
   * 解析属性泛型
   * @return The field type as {@link Type}. If it has type parameters in the declaration,<br>
   *         they will be resolved to the actual runtime {@link Type}s.
   */
  public static Type resolveFieldType(Field field, Type srcType) {
    Type fieldType = field.getGenericType();
    Class<?> declaringClass = field.getDeclaringClass();
    return resolveType(fieldType, srcType, declaringClass);
  }

  /**
   * 解析方法返回值的泛型
   * @return The return type of the method as {@link Type}. If it has type parameters in the declaration,<br>
   *         they will be resolved to the actual runtime {@link Type}s.
   */
  public static Type resolveReturnType(Method method, Type srcType) {
    Type returnType = method.getGenericReturnType();
    Class<?> declaringClass = method.getDeclaringClass();
    return resolveType(returnType, srcType, declaringClass);
  }

  /**
   * 解析方法输入参数的泛型
   * @return The parameter types of the method as an array of {@link Type}s. If they have type parameters in the declaration,<br>
   *         they will be resolved to the actual runtime {@link Type}s.
   */
  public static Type[] resolveParamTypes(Method method, Type srcType) {
    // 取出方法的所有输入参数
    Type[] paramTypes = method.getGenericParameterTypes();
    Class<?> declaringClass = method.getDeclaringClass();
    Type[] result = new Type[paramTypes.length];
    for (int i = 0; i < paramTypes.length; i++) {
      result[i] = resolveType(paramTypes[i], srcType, declaringClass);
    }
    return result;
  }

  /**
   * 解析变量的实际类型
   * @param type 变量的类型
   * @param srcType  变量所属的类
   * @param declaringClass  定义变量的类
   * @return 解析结果
   */
  private static Type resolveType(Type type, Type srcType, Class<?> declaringClass) {
    if (type instanceof TypeVariable) {
      return resolveTypeVar((TypeVariable<?>) type, srcType, declaringClass);
    } else if (type instanceof ParameterizedType) {
      return resolveParameterizedType((ParameterizedType) type, srcType, declaringClass);
    } else if (type instanceof GenericArrayType) {
      return resolveGenericArrayType((GenericArrayType) type, srcType, declaringClass);
    } else {
      return type;
    }
  }

  /**
   * 解析泛型列表的实际类型
   * @param genericArrayType  泛型列表变量类型
   * @param srcType 变量所属的类
   * @param declaringClass  定义变量的类
   * @return  解析结果
   */
  private static Type resolveGenericArrayType(GenericArrayType genericArrayType, Type srcType, Class<?> declaringClass) {
    Type componentType = genericArrayType.getGenericComponentType();
    Type resolvedComponentType = null;
    if (componentType instanceof TypeVariable) {  // 元素类型是类变量
      resolvedComponentType = resolveTypeVar((TypeVariable<?>) componentType, srcType, declaringClass);
    } else if (componentType instanceof GenericArrayType) { // 元素类型是泛型列表
      resolvedComponentType = resolveGenericArrayType((GenericArrayType) componentType, srcType, declaringClass);
    } else if (componentType instanceof ParameterizedType) {  // 元素类型是参数化类型
      resolvedComponentType = resolveParameterizedType((ParameterizedType) componentType, srcType, declaringClass);
    }
    if (resolvedComponentType instanceof Class) {
      return Array.newInstance((Class<?>) resolvedComponentType, 0).getClass();
    } else {
      return new GenericArrayTypeImpl(resolvedComponentType);
    }
  }

  /**
   * 解析参数化类型的实际结果
   * @param parameterizedType 参数化类型的变量
   * @param srcType   该变量所属的类
   * @param declaringClass  定义该变量的类
   * @return
   */
  private static ParameterizedType resolveParameterizedType(ParameterizedType parameterizedType, Type srcType, Class<?> declaringClass) {
    // 变量的原始类型。
    Class<?> rawType = (Class<?>) parameterizedType.getRawType();

    // 获取类型参数
    Type[] typeArgs = parameterizedType.getActualTypeArguments();

    // 获取参数的实际类型
    Type[] args = new Type[typeArgs.length];
    for (int i = 0; i < typeArgs.length; i++) {
      if (typeArgs[i] instanceof TypeVariable) {    //  类型参数是类型变量。
        args[i] = resolveTypeVar((TypeVariable<?>) typeArgs[i], srcType, declaringClass);
      } else if (typeArgs[i] instanceof ParameterizedType) {    // 类型参数是参数化类型。
        args[i] = resolveParameterizedType((ParameterizedType) typeArgs[i], srcType, declaringClass);
      } else if (typeArgs[i] instanceof WildcardType) {   //  类型参数是通配符泛型
        args[i] = resolveWildcardType((WildcardType) typeArgs[i], srcType, declaringClass);
      } else {  //  类型参数是确定的类型
        args[i] = typeArgs[i];
      }
    }
    return new ParameterizedTypeImpl(rawType, null, args);
  }

  private static Type resolveWildcardType(WildcardType wildcardType, Type srcType, Class<?> declaringClass) {
    Type[] lowerBounds = resolveWildcardTypeBounds(wildcardType.getLowerBounds(), srcType, declaringClass);
    Type[] upperBounds = resolveWildcardTypeBounds(wildcardType.getUpperBounds(), srcType, declaringClass);
    return new WildcardTypeImpl(lowerBounds, upperBounds);
  }

  private static Type[] resolveWildcardTypeBounds(Type[] bounds, Type srcType, Class<?> declaringClass) {
    Type[] result = new Type[bounds.length];
    for (int i = 0; i < bounds.length; i++) {
      if (bounds[i] instanceof TypeVariable) {
        result[i] = resolveTypeVar((TypeVariable<?>) bounds[i], srcType, declaringClass);
      } else if (bounds[i] instanceof ParameterizedType) {
        result[i] = resolveParameterizedType((ParameterizedType) bounds[i], srcType, declaringClass);
      } else if (bounds[i] instanceof WildcardType) {
        result[i] = resolveWildcardType((WildcardType) bounds[i], srcType, declaringClass);
      } else {
        result[i] = bounds[i];
      }
    }
    return result;
  }

  /**
   * 解析泛型变量的实际结果
   * @param typeVar 泛型变量
   * @param srcType 该变量所属的类
   * @param declaringClass  定义该变量的类
   * @return  泛型变量的实际结果
   */
  private static Type resolveTypeVar(TypeVariable<?> typeVar, Type srcType, Class<?> declaringClass) {
    // 解析出的泛型变量的结果
    Type result;
    Class<?> clazz;
    if (srcType instanceof Class) { // 该变量属于确定的类。
      clazz = (Class<?>) srcType;
    } else if (srcType instanceof ParameterizedType) {  // 该变量属于参数化的类型
      ParameterizedType parameterizedType = (ParameterizedType) srcType;
      clazz = (Class<?>) parameterizedType.getRawType();
    } else {
      throw new IllegalArgumentException("The 2nd arg must be Class or ParameterizedType, but was: " + srcType.getClass());
    }

    if (clazz == declaringClass) { // 变量所属的类和定义变量的类一致。
      // 确定泛型变量的上界
      Type[] bounds = typeVar.getBounds();
      if (bounds.length > 0) {
        return bounds[0];
      }
      return Object.class;
    }

    // 获取变量所属类的父类
    Type superclass = clazz.getGenericSuperclass();
    // 扫描父类，查看能否确定边界
    result = scanSuperTypes(typeVar, srcType, declaringClass, clazz, superclass);
    if (result != null) {
      return result;
    }

    // 获取变量所属类的接口
    Type[] superInterfaces = clazz.getGenericInterfaces();
    for (Type superInterface : superInterfaces) {
      result = scanSuperTypes(typeVar, srcType, declaringClass, clazz, superInterface);
      if (result != null) {
        return result;
      }
    }
    return Object.class;
  }

  private static Type scanSuperTypes(TypeVariable<?> typeVar, Type srcType, Class<?> declaringClass, Class<?> clazz, Type superclass) {
    if (superclass instanceof ParameterizedType) {
      ParameterizedType parentAsType = (ParameterizedType) superclass;
      Class<?> parentAsClass = (Class<?>) parentAsType.getRawType();
      TypeVariable<?>[] parentTypeVars = parentAsClass.getTypeParameters();
      if (srcType instanceof ParameterizedType) {
        parentAsType = translateParentTypeVars((ParameterizedType) srcType, clazz, parentAsType);
      }
      if (declaringClass == parentAsClass) {
        for (int i = 0; i < parentTypeVars.length; i++) {
          if (typeVar == parentTypeVars[i]) {
            return parentAsType.getActualTypeArguments()[i];
          }
        }
      }
      if (declaringClass.isAssignableFrom(parentAsClass)) {
        return resolveTypeVar(typeVar, parentAsType, declaringClass);
      }
    } else if (superclass instanceof Class && declaringClass.isAssignableFrom((Class<?>) superclass)) {
      return resolveTypeVar(typeVar, superclass, declaringClass);
    }
    return null;
  }

  private static ParameterizedType translateParentTypeVars(ParameterizedType srcType, Class<?> srcClass, ParameterizedType parentType) {
    Type[] parentTypeArgs = parentType.getActualTypeArguments();
    Type[] srcTypeArgs = srcType.getActualTypeArguments();
    TypeVariable<?>[] srcTypeVars = srcClass.getTypeParameters();
    Type[] newParentArgs = new Type[parentTypeArgs.length];
    boolean noChange = true;
    for (int i = 0; i < parentTypeArgs.length; i++) {
      if (parentTypeArgs[i] instanceof TypeVariable) {
        for (int j = 0; j < srcTypeVars.length; j++) {
          if (srcTypeVars[j] == parentTypeArgs[i]) {
            noChange = false;
            newParentArgs[i] = srcTypeArgs[j];
          }
        }
      } else {
        newParentArgs[i] = parentTypeArgs[i];
      }
    }
    return noChange ? parentType : new ParameterizedTypeImpl((Class<?>)parentType.getRawType(), null, newParentArgs);
  }

  private TypeParameterResolver() {
    super();
  }

  static class ParameterizedTypeImpl implements ParameterizedType {
    private Class<?> rawType;

    private Type ownerType;

    private Type[] actualTypeArguments;

    public ParameterizedTypeImpl(Class<?> rawType, Type ownerType, Type[] actualTypeArguments) {
      super();
      this.rawType = rawType;
      this.ownerType = ownerType;
      this.actualTypeArguments = actualTypeArguments;
    }

    @Override
    public Type[] getActualTypeArguments() {
      return actualTypeArguments;
    }

    @Override
    public Type getOwnerType() {
      return ownerType;
    }

    @Override
    public Type getRawType() {
      return rawType;
    }

    @Override
    public String toString() {
      return "ParameterizedTypeImpl [rawType=" + rawType + ", ownerType=" + ownerType + ", actualTypeArguments=" + Arrays.toString(actualTypeArguments) + "]";
    }
  }

  static class WildcardTypeImpl implements WildcardType {
    private Type[] lowerBounds;

    private Type[] upperBounds;

    WildcardTypeImpl(Type[] lowerBounds, Type[] upperBounds) {
      super();
      this.lowerBounds = lowerBounds;
      this.upperBounds = upperBounds;
    }

    @Override
    public Type[] getLowerBounds() {
      return lowerBounds;
    }

    @Override
    public Type[] getUpperBounds() {
      return upperBounds;
    }
  }

  static class GenericArrayTypeImpl implements GenericArrayType {
    private Type genericComponentType;

    GenericArrayTypeImpl(Type genericComponentType) {
      super();
      this.genericComponentType = genericComponentType;
    }

    @Override
    public Type getGenericComponentType() {
      return genericComponentType;
    }
  }
}
