package org.zfin.security;

import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 * This is a simple class for testing the performance of bcrypt on your machine.
 * We should choose a number of rounds such that the server takes about 1 second to authenticate.
 */
public class BcryptPerformanceBenchmark {
	public static int checkTimes = 1;

	public static void main(String[] args) {
//        String password = args[0];
		String password = "somerandom@%EAfs#,.";

		for (int i = 3; i <= 31; i++) {
			System.out.println("Computing performance of hash for " + i + " number of rounds.  Running check " + checkTimes + " times.");
			try {
				outputPerformanceInfo(password, i);
			} catch (IllegalArgumentException iae) {
				System.out.println(" Illegal arg");
			}
		}
	}

	private static void outputPerformanceInfo(String password, int numRounds) {
		String hash = BCrypt.hashpw(password, BCrypt.gensalt(numRounds));
		System.out.println(hash);
		long start = System.currentTimeMillis();
		for (int i = 0; i < checkTimes; i++) {
			BCrypt.checkpw(password, hash);
		}
		long end = System.currentTimeMillis();
		System.out.println(" Time: " + (end - start) + "\n");
	}

}