package org.hongxi.whatsmars.spring.context.annotation.service;

import org.hongxi.whatsmars.spring.context.annotation.repository.DemoRepository;
import org.springframework.stereotype.Service;

/**
 * Demonstrates constructor-based dependency injection.
 * Spring will inject {@link DemoRepository} via the constructor automatically.
 */
@Service
public class DemoService {

    private final DemoRepository demoRepository;

    public DemoService(DemoRepository demoRepository) {
        this.demoRepository = demoRepository;
        System.out.println("[DemoService] created with repository: " + demoRepository);
    }

    public void service() {
        System.out.println("[DemoService] service() called");
        demoRepository.query();
    }
}
