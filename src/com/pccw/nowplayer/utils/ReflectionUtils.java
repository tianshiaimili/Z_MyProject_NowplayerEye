package com.pccw.nowplayer.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * A helper class to handle Java Reflection Mechanism.
 * 
 * @author AlfredZhong
 * @version 2012-09-11
 * @version 2012-09-12, added inner class methods.
 */
public class ReflectionUtils {

	private ReflectionUtils() {
	}
	
	public static Class<?> getClass(String className) throws Exception {
		/*
		 * Same as Class.forName(className, true, currentLoader).
		 * throw ClassNotFoundException if the class cannot be located.
		 */
		return Class.forName(className);
	}
	
	/**
	 * <pre>
	 * Note:
	 * 1. isPublicConstructor if you don't know, set it false.
	 * 2. constructorArgsTypes distinguish primitive type and Wrapper Class:
	 *    e.g. int.class & Integer.class.
	 * </pre>
	 */
	public static Object getClassInstance(Class<?> clazz, boolean isPublicConstructor,
			Class<?>[] constructorArgsTypes, Object... constructorArgs) throws Exception {
		/*
		 * throw NoSuchMethodException if a matching method is not found.
		 * throw SecurityException if security problem occurs.
		 */
		Constructor<?> constructor;
		if(isPublicConstructor) {
			constructor = clazz.getConstructor(constructorArgsTypes);
		} else {
			constructor = clazz.getDeclaredConstructor(constructorArgsTypes);
		}
		if(!constructor.isAccessible()) {
			/*
			 * Disable Java language access checks.
			 * Default setting is enabled -- accessible false.
			 * Note that isAccessible() has nothing to do with public or private and so on:
			 * even public, if you don't setAccessible(true), isAccessible() returns false
			 * which means it will do Java language access checks.
			 */
			constructor.setAccessible(true);
		}
		/*
		 * throw InstantiationException if the class cannot be instantiated
		 * throw IllegalAccessException if this constructor is not accessible
		 * throw IllegalArgumentException if an incorrect number of arguments are passed, 
		 *       or an argument could not be converted by a widening conversion
		 * throw InvocationTargetException if an exception was thrown by the invoked constructor
		 */
		return constructor.newInstance(constructorArgs);
	}
	
	/**
	 * e.g. getInnerClass("com.kingwaystudio.OuterClass", "InnerClass");
	 */
	public static Class<?> getInnerClass(String outerClassName, String innerClassSimpleName) throws Exception {
		return getClass(outerClassName + "$" + innerClassSimpleName);
	}
	
	/**
	 * <pre>
	 * Note:
	 * 1. if inner class in a non-static context, you should:
	 *    add outer class type in constructorArgsTypes as the first parameter
	 *    add outer class instance in constructorArgs as the first parameter.
	 * </pre>
	 * @see #getClassInstance(Class, boolean, Class[], Object...)
	 */
	public static Object getInnerClassInstance(Class<?> clazz, boolean isPublicConstructor,
			Class<?>[] constructorArgsTypes, Object... constructorArgs) throws Exception {
		return getClassInstance(clazz, isPublicConstructor, constructorArgsTypes, constructorArgs);
	}
	
	/**
	 * <pre>
	 * Note:
	 * 1. fieldDeclaredInClass if you don't know, set it false.
	 * </pre>
	 */
	public static Field getField(Class<?> clazz, boolean fieldDeclaredInClass, String fieldName) throws Exception {
		/*
		 * throw SecurityException
		 * throw NoSuchFieldException if a field with the specified name is not found.
		 */
		if(fieldDeclaredInClass) {
			return clazz.getDeclaredField(fieldName);
		}
		return clazz.getField(fieldName);
	}
	
	/** 
	 * <pre>
	 * Note:
	 * 1. methodDeclaredInClass if you don't know, set it false.
	 * 2. methodArgsTypes distinguish primitive type and Wrapper Class:
	 *    e.g. int.class & Integer.class
	 * </pre>
	 */
	public static Method getMethod(Class<?> clazz, boolean methodDeclaredInClass, 
			String methodName, Class<?>... methodArgsTypes) throws Exception {
		Method method;
		if(methodDeclaredInClass) {
			/*
			 * throw NoSuchMethodException if a matching method is not found.
			 * throw SecurityException
			 */
			method = clazz.getDeclaredMethod(methodName, methodArgsTypes);
		} else {
			// throw same exceptions with Class.getDeclaredMethod().
			method = clazz.getMethod(methodName, methodArgsTypes);
		}
		if(!method.isAccessible()) {
			method.setAccessible(true);
		}
		return method;
	}
	
	public static Object getStaticFieldValue(Field field) throws Exception {
		if(!field.isAccessible()) {
			field.setAccessible(true);
		}
		return field.get(null);
	}
	
	public static Object getInstanceFieldValue(Object instance, Field field) throws Exception {
		if(!field.isAccessible()) {
			field.setAccessible(true);
		}
		return field.get(instance);
	}
	
	public static Object invokeStaticMethod(Method method, Object... methodArgs) throws Exception {
		if(!method.isAccessible()) {
			method.setAccessible(true);
		}
		/*
		 * throw IllegalArgumentException if
		 * the number of arguments doesn't match the number of parameters, 
		 * the receiver is incompatible with the declaring class, 
		 * or an argument could not be unboxed
		 * or converted by a widening conversion to the corresponding parameter type
		 * 
		 * throw IllegalAccessException if this method is not accessible.
		 * throw InvocationTargetException if an exception was thrown by the invoked method.
		 */
		return method.invoke(null, methodArgs);
	}
	
	public static Object invokeInstanceMethod(Object instance, Method method, Object... methodArgs) throws Exception {
		if(!method.isAccessible()) {
			method.setAccessible(true);
		}
		return method.invoke(instance, methodArgs);
	}

}
