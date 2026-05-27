/**
 * Created by shenhongxi on 2017/6/21.
 */
package org.hongxi.whatsmars.dubbo.demo.api;

import org.hongxi.whatsmars.dubbo.demo.api.vo.User;

import java.util.concurrent.CompletableFuture;

public interface DemoService {

	String sayHello(String name);

	User echo(User user);

	default CompletableFuture<String> sayHelloAsync(String name) {
		return CompletableFuture.completedFuture(sayHello(name));
	}
}