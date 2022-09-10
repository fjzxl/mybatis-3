/**
 *    Copyright 2009-2015 the original author or authors.
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * @author Clinton Begin
 */
public class ExceptionUtil {

  private ExceptionUtil() {
    // Prevent Instantiation
  }

  /**
   * 拆解java.lang.reflect.InvocationTargetException和java.lang.reflect.UndeclaredThrowableException异常，得到真正异常
   * InvocationTargetException是反射操作时统一抛出的异常
   * 真正的异常保存在内部的target属性中
   * 这样使得反射操作的异常有一个同一的界面，方便异常管理
   * @param wrapped
   * @return
   */
  public static Throwable unwrapThrowable(Throwable wrapped) {
    Throwable unwrapped = wrapped;
    while (true) {
      if (unwrapped instanceof InvocationTargetException) {
        unwrapped = ((InvocationTargetException) unwrapped).getTargetException();
      } else if (unwrapped instanceof UndeclaredThrowableException) {
        unwrapped = ((UndeclaredThrowableException) unwrapped).getUndeclaredThrowable();
      } else {
        return unwrapped;
      }
    }
  }

}
