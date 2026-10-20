package com.restaurant.api;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurant.demo.BeanContainerDemo;
import com.restaurant.demo.HibernateSessionDemo;

/** Endpoints that run the short concept demos (for the viva). */
@RestController
@RequestMapping("/api/demo")
public class DemoRestController {

    private final HibernateSessionDemo hibernateSessionDemo;
    private final BeanContainerDemo beanContainerDemo;

    public DemoRestController(HibernateSessionDemo hibernateSessionDemo, BeanContainerDemo beanContainerDemo) {
        this.hibernateSessionDemo = hibernateSessionDemo;
        this.beanContainerDemo = beanContainerDemo;
    }

    /** SessionFactory -> Session -> Transaction -> HQL */
    @GetMapping("/hibernate")
    public Map<String, Object> hibernate() {
        return hibernateSessionDemo.run();
    }

    /** BeanFactory (lazy) vs ApplicationContext (eager) + dependency injection */
    @GetMapping("/beans")
    public Map<String, Object> beans() {
        return beanContainerDemo.run();
    }
}
