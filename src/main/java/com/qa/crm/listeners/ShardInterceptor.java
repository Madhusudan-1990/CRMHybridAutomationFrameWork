package com.qa.crm.listeners;

import java.util.ArrayList;
import java.util.List;

import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

/**
 * Splits the suite across N pods.
 *
 * Kubernetes runs an indexed Job with parallelism=N, so every pod gets a
 * JOB_COMPLETION_INDEX from 0 to N-1. Each pod then keeps only the methods where
 * methodIndex % N == shardIndex, so the pods cover disjoint slices of the suite
 * instead of all running the same tests against the same application.
 *
 * Defaults to a single shard, so a laptop run is unaffected:
 *   mvn test                                    -> all methods, one shard
 *   mvn test -Dshard.count=4 -Dshard.index=2    -> every 4th method, starting at 2
 */
public class ShardInterceptor implements IMethodInterceptor {

	@Override
	public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {

		int shardIndex = readInt("SHARD_INDEX", "shard.index", 0);
		int shardCount = readInt("SHARD_COUNT", "shard.count", 1);

		if (shardCount <= 1 || shardIndex < 0 || shardIndex >= shardCount) {
			System.out.println("Sharding off: running all " + methods.size() + " methods");
			return methods;
		}

		List<IMethodInstance> shard = new ArrayList<>();
		for (int i = 0; i < methods.size(); i++) {
			//every Nth method goes to this pod
			if (i % shardCount == shardIndex) {
				shard.add(methods.get(i));
			}
		}

		System.out.println("Shard " + shardIndex + " of " + shardCount
				+ " -> " + shard.size() + " of " + methods.size() + " methods");
		return shard;
	}

	/**
	 * Reads an int from the env var first (docker/k8s), then the system property (mvn -D).
	 */
	private int readInt(String envKey, String propKey, int defaultValue) {
		String value = System.getenv(envKey);
		if (value == null || value.trim().isEmpty()) {
			value = System.getProperty(propKey);
		}
		if (value == null || value.trim().isEmpty()) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			System.out.println("Invalid value for " + envKey + " = " + value + ", using " + defaultValue);
			return defaultValue;
		}
	}
}
