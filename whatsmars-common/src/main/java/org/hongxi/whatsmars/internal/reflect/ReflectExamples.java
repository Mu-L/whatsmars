package org.hongxi.whatsmars.internal.reflect;

import java.lang.reflect.*;
import java.util.Arrays;

/**
 * 反射与动态代理示例：Class获取、字段/方法操作、JDK动态代理、CGLIB原理
 */
class ReflectExamples {

    public static void main(String[] args) throws Exception {
        classInfoDemo();
        fieldAccess();
        methodInvoke();
        jdkProxy();
    }

    // ==================== 1. 获取 Class 对象的四种方式 ====================

    static void classInfoDemo() throws Exception {
        System.out.println("===== Class 信息 =====");

        // 1. Class.forName("全限定名") - 最常用，框架中大量使用（如 JDBC 加载驱动）
        Class<?> c1 = Class.forName("java.util.ArrayList");

        // 2. 类名.class - 编译时确定，不会触发类初始化
        Class<?> c2 = java.util.ArrayList.class;

        // 3. 实例.getClass() - 运行时获取
        Class<?> c3 = new java.util.ArrayList<>().getClass();

        // 4. 类加载器（了解即可）
        // Class<?> c4 = classLoader.loadClass("java.util.ArrayList");

        System.out.println("三种方式获取同一Class: " + (c1 == c2 && c2 == c3));

        // 通过 Class 获取类信息
        Class<?> clazz = SampleService.class;
        System.out.println("类名: " + clazz.getName());
        System.out.println("简单名: " + clazz.getSimpleName());
        System.out.println("包名: " + clazz.getPackageName());
        System.out.println("父类: " + clazz.getSuperclass().getSimpleName());
        System.out.println("接口: " + Arrays.toString(clazz.getInterfaces()));
        System.out.println("构造器: " + Arrays.toString(clazz.getDeclaredConstructors()));

        // 方法列表
        System.out.println("声明的方法:");
        for (Method m : clazz.getDeclaredMethods()) {
            System.out.printf("  %s %s(%s)%n",
                Modifier.toString(m.getModifiers()),
                m.getName(),
                Arrays.toString(m.getParameterTypes()));
        }
        System.out.println();
    }

    // ==================== 2. 字段访问（包括私有字段）====================

    static void fieldAccess() throws Exception {
        System.out.println("===== 字段访问 =====");

        SampleService service = new SampleService();

        // 获取私有字段
        Field secretField = SampleService.class.getDeclaredField("secret");
        secretField.setAccessible(true); // 突破访问控制（反射的核心能力）

        // 读取
        System.out.println("secret原值: " + secretField.get(service));

        // 修改
        secretField.set(service, "modified-by-reflection");
        System.out.println("secret修改后: " + secretField.get(service));

        // 注意：setAccessible(true) 在模块化系统（JDK 9+）中可能受限
        // 框架中大量使用：Spring 注入私有字段、MyBatis 映射私有属性
        System.out.println();
    }

    // ==================== 3. 方法调用 ====================

    static void methodInvoke() throws Exception {
        System.out.println("===== 方法调用 =====");

        SampleService service = new SampleService();

        // 调用无参方法
        Method greetMethod = SampleService.class.getDeclaredMethod("greet");
        String result = (String) greetMethod.invoke(service);
        System.out.println("invoke greet: " + result);

        // 调用带参方法
        Method addMethod = SampleService.class.getDeclaredMethod("add", int.class, int.class);
        int sum = (int) addMethod.invoke(service, 3, 5);
        System.out.println("invoke add(3,5): " + sum);

        // 调用私有静态方法
        Method privateMethod = SampleService.class.getDeclaredMethod("privateHelper");
        privateMethod.setAccessible(true);
        privateMethod.invoke(null); // 静态方法传 null

        // 反射性能：
        // - 首次调用较慢（需要类加载、方法查找、安全检查）
        // - 可通过 setAccessible(true) 跳过安全检查提速
        // - MethodHandle（JDK 7+）性能更好，可编译为字节码
        System.out.println();
    }

    // ==================== 4. JDK 动态代理 ====================

    static void jdkProxy() throws Exception {
        System.out.println("===== JDK 动态代理 =====");

        // 动态代理是 Spring AOP、RPC 框架的基石
        // 要求：目标类必须实现接口

        // 创建真实对象
        UserService realService = new UserServiceImpl();

        // 创建代理对象
        UserService proxy = (UserService) Proxy.newProxyInstance(
            UserServiceImpl.class.getClassLoader(),
            UserServiceImpl.class.getInterfaces(),
            new LoggingHandler(realService)
        );

        // 调用代理方法（会自动触发 Handler）
        String user = proxy.getUser("user-001");
        System.out.println("代理返回: " + user);

        proxy.saveUser("new-user");

        // JDK代理 vs CGLIB：
        // - JDK代理：基于接口，使用 java.lang.reflect.Proxy
        // - CGLIB：基于继承，生成目标类的子类（不需要接口）
        // - Spring 默认策略：有接口用JDK代理，无接口用CGLIB
        System.out.println();
    }

    // ---------- 辅助类 ----------

    static class SampleService {
        private String secret = "original-secret";

        String greet() {
            return "Hello from SampleService!";
        }

        int add(int a, int b) {
            return a + b;
        }

        private static void privateHelper() {
            System.out.println("私有静态方法被调用");
        }
    }

    // 接口
    interface UserService {
        String getUser(String id);
        void saveUser(String name);
    }

    // 实现类
    static class UserServiceImpl implements UserService {
        @Override
        public String getUser(String id) {
            return "User-" + id;
        }

        @Override
        public void saveUser(String name) {
            System.out.println("保存用户: " + name);
        }
    }

    // InvocationHandler：代理逻辑
    static class LoggingHandler implements InvocationHandler {
        private final Object target;

        LoggingHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("[前置] 调用 " + method.getName() +
                " 参数: " + Arrays.toString(args));
            long start = System.nanoTime();

            Object result = method.invoke(target, args);

            long cost = (System.nanoTime() - start) / 1000;
            System.out.println("[后置] " + method.getName() + " 耗时: " + cost + "μs");

            return result;
        }
    }
}
