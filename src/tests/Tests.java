package tests;
@SuppressWarnings("unused")
public class Tests {
	// XXX: test1 DEBE quedar fijo, porque se mira el numero de linea.
	public void test1() {
		ClassA a = new ClassA();
	}
	
	public void test2() {
		ClassA a = new ClassA();
		ClassA a2 = a;
	}
	
	public void test3() {
		ClassA a = new ClassA();
		a.classBAttribute = new ClassB();
		ClassB b = new ClassB();
		b = a.classBAttribute;
	}

	public void test4() {
		ClassA a = new ClassA();
		ClassB b = new ClassB();
		a.classBAttribute = b;
	}
	
	public void test5() {
		ClassA a = new ClassA();
		ClassA a2 = new ClassA();
		a2.classBAttribute = new ClassB();
		a.classBAttribute = a2.classBAttribute;
	}

	public void test6() {
		ClassA a = new ClassA(), a2 = new ClassA();
	}
	
	public void test7() {
		ClassA a = new ClassA();
		for (int i = 0; i < 5; i++) {
			ClassA a2 = new ClassA();
			a = a2;
		}
	}
	
	public void test8() {
		ClassA a = new ClassA();
		int x = 1;
		if (x > 2) {
			a = new ClassA();
		} else {
			a = new ClassA();
		}
	}
	
	public void test9() {
		ClassA a = new ClassA();
		a.classAAttribute = new ClassA();
		a.classAAttribute = new ClassA();
		ClassA a2 = new ClassA();
		a2 = a;
		a2 = a.classAAttribute;
	}

	public void test10(Object x, Object y) {}

	public void test11() {
		ClassA a = new ClassA();
		a.classBAttribute = new ClassB();
		ClassB b = new ClassB();
		ClassB b2 = new ClassB();
		b = a.classBAttribute;
		b2 = a.classBAttribute;
	}

	public void test12() {
		TestClassC c = TestClassC.getInstance(); 
	}

	public void test13() {
		Object o = new Object();
		Object a = modify(o);
	}

	public void test14() {
		ClassA a = new ClassA();
		int x = 1;
		if (x > 2) {
			a = new ClassA();
			return;
		} else {
			a = new ClassA();
			return;
		}
	}

	/* XXX: Por el new esto tiene que quedar siempre en linea 99!!*/
	private Object modify(Object x) {
		x = new Object();
		return x;
	}

	public void test15() {
		ClassA a = new ClassA();
		a.ObjectAttribute = new Object();
		a.ObjectAttribute = modify(a);
	}

	public ClassA classAAttribute; // Si lo instancio aca no aparece en el analisis.
	public void test16() {
		classAAttribute = new ClassA();
	}

	public void test17() {
		TestClassC c = TestClassC.getInstance();
	}

	public void test18() {
		staticAAttribute = new ClassA();
		ClassA a = staticAAttribute;
	}

	public void test19() {
		instanceAAttribute = new ClassA();
		ClassA a = instanceAAttribute;
	}

	public static ClassA instanceAAttribute;
	public static ClassA staticAAttribute;
	
	public class ClassA {
		public ClassA() {}
		public ClassB classBAttribute;
		public ClassA classAAttribute;
		public Object ObjectAttribute;
		public void initClassBAttribute() {
			classBAttribute = new ClassB();
		}
	}

	public void test20() {
		ClassA a = new ClassA();
		a.initClassBAttribute();
		ClassB b = a.classBAttribute;
	}

	public class ClassB {
		public ClassB() {}
		public ClassA classAAttribute;
		public void func() {
			ClassA a = new ClassA();
			a.initClassBAttribute();
			classAAttribute = a;
		}
	}

	public void test21() {
		ClassB b = classBFromConstructor();
	}

	private ClassB classBFromConstructor() {
		ClassB b = new ClassB();
		b.func();
		ClassB b2 = b.classAAttribute.classBAttribute;
		return b2;
	}

	public void test22() {
		TestClassC c = new TestClassC();
		int y = c.recursive(2);
	}

	public void test23() {
		ClassA a = new ClassA();
		a.initClassBAttribute();
	}

	public void test24() {
		ClassA a = new ClassA();
		a.initClassBAttribute();
		a.initClassBAttribute();
	}

	public void test25() {
		int x = 1;
		Animal a = null;
		if (x > 2) {
			a = new Dog();
		} else {
			a = new Cat();
		}
		int z = a.lives();
	}

	public void test26() {
		ClassB b = classBFromConstructor();
	}

	public void modifySelf() {
		classAAttribute = new ClassA();
	}
	
	public void test27() {
		modifySelf();
		ClassA a = classAAttribute;
	}

	public void test28() {
		ClassA a = new ClassA();
		TestClassC c = new TestClassC(a);
		ClassA a2 = c.classAAttribute;
	}

	class ClassD {
		public ClassA returnClassA() {
			ClassA a = new ClassA();
			return a;
		}
	}
	
	public void test29() {
		ClassD d = new ClassD();
		ClassA a = null;
		for (int i = 0; i < 4; i++) {
			a = d.returnClassA();
		}
		ClassA a2 = a;
	}
	
	public void test30() {
		ClassA a = new ClassA();
		func1(a);
		ClassB b = a.classBAttribute;
	}
	
	public void func1(ClassA a) {
		func2(a);
	}
	
	public void func2(ClassA a) {
		a.classBAttribute = new ClassB();
	}
}