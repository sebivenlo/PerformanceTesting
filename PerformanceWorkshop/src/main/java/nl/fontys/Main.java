package nl.fontys;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

    public static void main(String[] args) {
        ApplicationContext appContext = new ClassPathXmlApplicationContext("context.xml");
        
        AwesomeWebService webService = (AwesomeWebService) appContext.getBean("awesomeWebService");
        webService.setProxy(webService);
        webService.getAwesomeData();
    }
}
