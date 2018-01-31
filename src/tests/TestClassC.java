package tests;

import tests.Tests.ClassA;

public class TestClassC {
	private static TestClassC instance = null;
	protected TestClassC() {}

	public static TestClassC getInstance() {
		if(instance == null) {
			instance = new TestClassC();
		}
		return instance;
	}

	public ClassA classAAttribute;
	public TestClassC(ClassA a) {
		classAAttribute = a;
	}
	
	public int recursive(int i) {
		int ret = 2;
		if (i > 0) {
			i--;
			ret = recursive(i) * 2;
		}
		return ret;
	}
}