/**
 * Created by shenhongxi on 2017/6/21.
 */
package org.hongxi.whatsmars.dubbo.demo.api;

import java.util.concurrent.CompletableFuture;

public interface DemoService {

	String sayHello(String name);

	default CompletableFuture<String> sayHelloAsync(String name) {
		return CompletableFuture.completedFuture(sayHello(name));
	}
}