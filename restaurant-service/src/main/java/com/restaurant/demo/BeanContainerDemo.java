package com.restaurant.demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import com.restaurant.service.MenuService;

/**
 * BeanFactory vs ApplicationContext (the two Spring IoC containers).
 *
 *  BeanFactory        - the BASIC container. Creates beans LAZILY (only when getBean() is called)
 *                       and does dependency injection. Nothing more.
 *  ApplicationContext - extends BeanFactory and adds: EAGER creation of singletons at start-up,
 *                       events, internationalisation (messages), @Autowired/annotation processing,
 *                       AOP integration, environment/properties. Spring Boot always uses an
 *                       ApplicationContext.
 */
@Component
public class BeanContainerDemo {

    private final ApplicationContext applicationContext;

    // DI: Spring injects its own ApplicationContext
    public BeanContainerDemo(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Map<String, Object> run() {
        Map<String, Object> result = new LinkedHashMap<>();

        // ---- 1. Plain BeanFactory: lazy ----
        DemoBean.clearLog();
        List<String> beanFactorySteps = new ArrayList<>();
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        RootBeanDefinition definition = new RootBeanDefinition(DemoBean.class);
        definition.getConstructorArgumentValues().addGenericArgumentValue("bean-from-BeanFactory");
        beanFactory.registerBeanDefinition("demoBean", definition);
        beanFactorySteps.add("Bean definition registered. Objects created so far: " + DemoBean.getCreationLog());
        DemoBean fromFactory = beanFactory.getBean("demoBean", DemoBean.class);
        beanFactorySteps.add("After getBean(): " + DemoBean.getCreationLog() + " -> " + fromFactory.greet());
        result.put("beanFactory (lazy)", beanFactorySteps);

        // ---- 2. ApplicationContext: eager singletons ----
        DemoBean.clearLog();
        List<String> contextSteps = new ArrayList<>();
        try (GenericApplicationContext context = new GenericApplicationContext()) {
            RootBeanDefinition def2 = new RootBeanDefinition(DemoBean.class);
            def2.getConstructorArgumentValues().addGenericArgumentValue("bean-from-ApplicationContext");
            context.registerBeanDefinition("demoBean", def2);
            contextSteps.add("Bean definition registered. Objects created so far: " + DemoBean.getCreationLog());
            context.refresh(); // starts the container
            contextSteps.add("After refresh() (before any getBean!): " + DemoBean.getCreationLog());
            contextSteps.add(context.getBean(DemoBean.class).greet());
        }
        result.put("applicationContext (eager)", contextSteps);

        // ---- 3. The real ApplicationContext of this running application ----
        Map<String, Object> app = new LinkedHashMap<>();
        app.put("contextClass", applicationContext.getClass().getSimpleName());
        app.put("totalBeans", applicationContext.getBeanDefinitionCount());
        app.put("ourBeans", Arrays.stream(applicationContext.getBeanDefinitionNames())
                .filter(name -> {
                    Class<?> type = applicationContext.getType(name);
                    return type != null && type.getName().startsWith("com.restaurant");
                })
                .sorted()
                .toList());
        MenuService first = applicationContext.getBean(MenuService.class);
        MenuService second = applicationContext.getBean(MenuService.class);
        app.put("menuServiceIsSingleton (same object twice)", first == second);
        app.put("menuServiceClass (CGLIB proxy because of AOP/@Transactional)", first.getClass().getSimpleName());
        result.put("thisApplication", app);
        return result;
    }
}
