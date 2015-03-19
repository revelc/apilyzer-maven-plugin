package aaa;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import org.apache.accumulo.core.data.ByteSequence;
import org.apache.accumulo.core.data.Column;
import org.apache.accumulo.core.data.Condition;
import org.apache.accumulo.core.data.ConditionalMutation;
import org.apache.accumulo.core.data.Key;
import org.apache.accumulo.core.data.Mutation;
import org.apache.accumulo.core.data.PartialKey;
import org.apache.accumulo.core.data.Range;
import org.apache.accumulo.core.data.Value;
import org.apache.accumulo.core.security.Authorizations;
import org.apache.accumulo.core.security.ColumnVisibility;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

public class Analyze
{

  static String format = "%-20s %-60s %-35s %-35s\n";

  public static TreeSet<String> nonAccumulo = new TreeSet<String>();
  public static TreeSet<String> nonPublic = new TreeSet<String>();

  public static String shorten(String s){
    return s.replace("org.apache.accumulo.core.client", "o.a.a.c.c").replace("org.apache.accumulo.core", "o.a.a.c").replace("org.apache.hadoop", "o.a.h").replace("org.apache.accumulo", "o.a.a");
  }

  public static boolean isOk(HashSet<String> publicSet, Class<?> clazz) {

    while (clazz.isArray()) {
      clazz = clazz.getComponentType();
    }

    if (clazz.isPrimitive()) {
      return true;
    }

    if(publicSet.contains(clazz.getName())){
      return true;
    }

    String pkg = clazz.getPackage().getName();

    if(!pkg.startsWith("org.apache.accumulo")){
      if(!pkg.startsWith("java")){
        nonAccumulo.add(clazz.getName());
      }
      return true;
    }

    nonPublic.add(clazz.getName());

    return false;
  }

  public static void checkClass(Class<?> clazz, HashSet<String> publicSet) throws Exception {

    if(clazz.isAnnotationPresent(Deprecated.class)){
      return;
    }

    Field[] fields = clazz.getFields();
    for (Field field : fields) {
      if (!isOk(publicSet, field.getType())) {
        System.out.printf(format, "Field", shorten(clazz.getName()), field.getName(), shorten(field.getType().getName()));
      }
    }

    Constructor<?>[] constructors = clazz.getConstructors();
    for (Constructor<?> constructor : constructors) {
      if (constructor.isAnnotationPresent(Deprecated.class))
        continue;

      Class<?>[] params = constructor.getParameterTypes();
      for (Class<?> param : params) {
        if (!isOk(publicSet, param)) {
          System.out.printf(format, "Constructor param", shorten(clazz.getName()), "(...)", shorten(param.getName()));
        }
      }
    }

    Method[] methods = clazz.getMethods();

    for (Method method : methods) {

      if (method.isAnnotationPresent(Deprecated.class))
        continue;

      if (!isOk(publicSet, method.getReturnType())) {
        System.out.printf(format, "Method return", shorten(clazz.getName()), method.getName()+"(...)", shorten(method.getReturnType().getName()));
      }

      Class<?>[] params = method.getParameterTypes();
      for (Class<?> param : params) {
        if (!isOk(publicSet, param)) {
          System.out.printf(format, "Method param", shorten(clazz.getName()), method.getName()+"(...)", shorten(param.getName()));
        }
      }
    }

    Class<?>[] classes = clazz.getClasses();
    for (Class<?> class1 : classes) {
      if(!isOk(publicSet, class1)){
        System.out.printf(format, "Public class", shorten(clazz.getName()), "N/A", shorten(class1.getName()));
      }
    }
  }

  public static void main(String[] args) throws Exception {

    ClassPath cp = ClassPath.from(Mutation.class.getClassLoader());

    List<Class<?>> publicAPIClasses = new ArrayList<Class<?>>();

    for (ClassInfo classInfo : cp.getTopLevelClassesRecursive("org.apache.accumulo.core.client")) {
      if(classInfo.getName().contains(".impl.") || classInfo.getName().endsWith("Impl"))
        continue;
      publicAPIClasses.add(classInfo.load());
    }

    for (ClassInfo classInfo : cp.getTopLevelClassesRecursive("org.apache.accumulo.minicluster")) {
      if(classInfo.getName().contains(".impl.") || classInfo.getName().endsWith("Impl"))
        continue;
      publicAPIClasses.add(classInfo.load());
    }

    //add specific classes not in client or minicluster package
    publicAPIClasses.add(Mutation.class);
    publicAPIClasses.add(Key.class);
    publicAPIClasses.add(Value.class);
    publicAPIClasses.add(Condition.class);
    publicAPIClasses.add(ConditionalMutation.class);
    publicAPIClasses.add(Range.class);
    publicAPIClasses.add(ColumnVisibility.class);
    publicAPIClasses.add(Authorizations.class);
    publicAPIClasses.add(ByteSequence.class);
    publicAPIClasses.add(PartialKey.class);
    publicAPIClasses.add(Column.class);

    //add subclasses of public API
    for (Class<?> clazz : new ArrayList<Class<?>>(publicAPIClasses)) {
      Class<?>[] declaredClasses = clazz.getDeclaredClasses();
      for (Class<?> declaredClazz : declaredClasses) {
        if((declaredClazz.getModifiers() & (Modifier.PUBLIC | Modifier.PROTECTED)) != 0)
          publicAPIClasses.add(declaredClazz);
      }
    }

    HashSet<String> publicSet = new HashSet<String>(Collections2.transform(publicAPIClasses, new Function<Class<?>,String>() {
      public String apply(Class<?> input) {
        return input.getName();
      }
    }));

    System.out.printf(format, "CONTEXT","TYPE","FIELD/METHOD","NON-PUBLIC REFERENCE");
    System.out.println();


    //look for public API methods/fields/subclasses that use classes not in public API
    for (Class<?> clazz : publicAPIClasses) {
      checkClass(clazz, publicSet);
    }

    System.out.println();
    System.out.println("Non Public API classes referenced in API : ");
    System.out.println();

    for(String clazz : nonPublic){
      System.out.println(clazz);
    }

    System.out.println();
    System.out.println("Non Accumulo classes referenced in API : ");
    System.out.println();

    for(String clazz : nonAccumulo){
      System.out.println(clazz);
    }

  }
}
