package logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

@Aspect
public class LoggingAspect {

    private int counter = 0;

//    @Before("execution(* nl.fontys.AwesomeWebService.getAwesomeData(..))")
//    public void logBeforeGetAwesomeData() {
//        System.out.println("before getAwesomeData");
//    }

//    @After("execution(* nl.fontys.AwesomeWebService.getAwesomeData(..))")
//    public void logAfterGetAwesomeData() {
//        System.out.println("after getAwesomeData");
//    }
    
    @Around("execution(* nl.fontys.AwesomeWebService.getAwesomeData(..))")
    public Object logAroundGetAwesomeData(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("before");
        Object result = new Object();
        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            throw ex;
        } finally {
            System.out.println("after");
            System.out.println("counter = " + counter);
        }
        return result;
    }

    @Before("execution(* nl.fontys.AwesomeWebService.magicMethod(..))")
    public void logBeforeMagicMethod() {
        counter++;
    }
}
