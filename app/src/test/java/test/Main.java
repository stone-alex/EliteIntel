package test;

import test.BindingsVerifier;

public class Main {
	public static void main(String[] args) {
		System.out.println("Hello World");

		BindingsVerifier bindings = BindingsVerifier.getInstance();
		bindings.getBindings();

	}
}