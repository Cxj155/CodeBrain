package com.codebrain.common;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@Component
public class ControllerChecker implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Map<String, Object> allController = event.getApplicationContext().getBeansWithAnnotation(RestController.class);
        System.out.println("\n=================== 已实例化Controller列表 ===================");
        if(allController.isEmpty()){
            System.out.println("无任何Controller Bean");
        }else {
            allController.forEach((beanName, obj) -> {
                System.out.println(beanName + " -> " + obj.getClass().getName());
            });
        }
        System.out.println("=========================================================\n");
    }
}