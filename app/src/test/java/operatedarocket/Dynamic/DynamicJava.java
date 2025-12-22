package operatedarocket.Dynamic;

import javax.tools.*;
import java.lang.reflect.*;

public class DynamicJava {
    public static void main(String[] args) throws Exception {
        String code =
                "public class Hello {" +
                        "  public static String run() { return \"Hello from runtime!\"; }" +
                        "}";

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        JavaFileObject file = new JavaSourceFromString("Hello", code);

        compiler.getTask(null, null, null, null, null, java.util.Arrays.asList(file)).call();

        Class<?> cls = Class.forName("Hello");
        Method m = cls.getMethod("run");
        System.out.println(m.invoke(null));
    }
}
