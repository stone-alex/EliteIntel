package test;

import test.BindingsVerifier;

public class Main {
	public static void main(String[] args) {
		System.out.println("Hello World");

		try {
			BindingsVerifier verifyBindings = BindingsVerifier.getInstance();
			//verifyBindings.getBindings();
			verifyBindings.testEnums();
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
}