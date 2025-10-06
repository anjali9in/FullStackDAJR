package com.core.fullstack.garbageC;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;

public class UnloadingClass {

	public static void main(String[] args) throws Exception {
		
		URL classDir = new URL("file:///C:/Users/AnjaliGupta/Documents/MyWorkspace/Learn"); //path to the directory containing .class file
		URLClassLoader customClassLoader = new URLClassLoader(new URL[] { classDir });
		
		Class<?> largeClass = Class.forName("BoolTests", true, customClassLoader); // class file name without .class extension

		WeakReference<ClassLoader> weakClassLoaderRef = new WeakReference<>(customClassLoader);

		customClassLoader = null;
		largeClass = null;

		for (int i = 0; i < 10; i++) {
			System.gc();
			Thread.sleep(500);
			if (weakClassLoaderRef.get() == null) {
				System.out.println("Custom class loader has been garbage collected, indicating that LargeClass might have been unloaded");
				return;
			}
		}
		System.out.println("Custom class loader is still in memory");
	}
}