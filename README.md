# Performance testing workshop
Ever wondered how fast your code actually is or how "good" a certain algorith solves a problem compared to an other.

> "Trust is good, but control is better." (Vladimir Lenin)

Measuring the actual performance of code can be difficult to do. In addition, more often than not, it required changes to the business logic to enable the measuring in the first place.

## The Problem
I want to measure how often a function is called or how long a method call takes.
**But:** I dont want to modify my existing business logic. This problem is called "Cross-cutting concern".

## Scenario
Lets think about a scenario here. You have an existing webservice that does some heavy computations. As more and more users access this service, the responses get slow. You want to find out what is taking so long.

**Solution: Logging**

## Step 1
Lets asume we have this webservice.

```java
import java.util.Random;

public class AwesomeWebService {

    private final Random random;

    public AwesomeWebService() {
        random = new Random();
    }

    public int getAwesomeData() {
        int runs = random.nextInt(300);
        for (int i = 0; i < runs; i++) {
            //Simulate expensive method
            try {
                Thread.sleep(random.nextInt(10) + 5);
            } catch (InterruptedException ex) {
            }
        }
        int result = random.nextInt();
        System.out.println("result = " + result);
        return result;
    }
}
```

```java
public class Main {

    public static void main(String[] args) {
        AwesomeWebService webService = new AwesomeWebService();
        webService.getAwesomeData();
    }
}
```
Create these classes in whatever editor you like and run the main **several times**. (It is recommended to already create a maven project because we need maven later.)
Notice the total time is takes to run varies from notime to ~ 3 seconds.

## Step 2
Now try to find out why sometimes it takes up to 3 times as long. These lines could help.

```java
	private int expensiceMethodCounter;
	expensiveMethodCounter++;
	System.out.println("Expensice method ran " + expensiveMethodCounter + " times.");
```

We now identified that there is some code which is called quite often sometimes and can take a long time.

However, we modified the actual source code of the implementation in order to allow this.
**-> Not desired**

## Aspect oriented programming (AOP)
Aspect oriented programming is a mechanism of seperating cross-cuting concerns. It allows to add additional behavior to existing code, without modifying the code itself.


## Step 3
Now, because we have created a maven project in Step 1, we can make use of some magic. (Not actual magic, but maven)

* Delete the three lines added in Step 2.
* Add the following maven dependencies to the pom file:

```xml
<dependencies>
  <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-aop</artifactId>
      <version>4.3.3.RELEASE</version>
  </dependency>
  <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-context</artifactId>
      <version>4.3.3.RELEASE</version>
  </dependency>
  <dependency>
      <groupId>org.springframework</groupId>
      <artifactId>spring-core</artifactId>
      <version>4.3.3.RELEASE</version>
  </dependency>
  <dependency>
      <groupId>cglib</groupId>
      <artifactId>cglib</artifactId>
      <version>3.2.4</version>
  </dependency>
  <dependency>
      <groupId>org.aspectj</groupId>
      <artifactId>aspectjrt</artifactId>
      <version>1.8.9</version>
  </dependency>
  <dependency>
      <groupId>org.aspectj</groupId>
      <artifactId>aspectjweaver</artifactId>
      <version>1.8.9</version>
  </dependency>
</dependencies>
```

* build with dependencies
* create a folder called `resources` in projectRoot/src/main/
* create an xml file in this folder with an arbritary name
* give it this content:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
	http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
	http://www.springframework.org/schema/aop
	http://www.springframework.org/schema/aop/spring-aop-3.0.xsd ">

    <aop:aspectj-autoproxy />

    <bean id="awesomeWebService" class="packagename.AwesomeWebService"/>

    <!-- Aspect -->
    <bean id="loggingAspect" class="logging.LoggingAspect" />
</beans>
```

* replace packagename with the name of your actual package
* create a package called `logging` in `Source packages`
* create a class called `LoggingAspect` in the `logging` package

Now you have the base to start using AOP.

## Step 4
Now we will make use of the spring AOP framework that we just added via maven.

##### Main class
* add `ApplicationContext appContext = new ClassPathXmlApplicationContext("example.xml");` as the first line in the main method
	* replace example with the name of your xml file
* now we have to get the bean that knows about AOP and use it as out webservice
* replace the constructor call of AwesomeWebService with `appContext.getBean("awesomeWebService");` and cast the result to an `AwesomeWebService`


##### LoggingAspect class
* Import these packages in `LoggingAspect`.

```java
import org.aspectj.lang.annotation.*;
```

* Annotate the class with `@Aspect`
* create `public void logBeforeGetAwesomeData()`
* annotate this method with `@Before("execution(* packagename.AwesomeWebService.getAwesomeData(..))")`
	* replace packagename with the name of your actual package
* now you can wrote some logging in the `logBeforeGetAwesomeData ` method
* it should apear on the console before the result `AwesomeWebService.getAwesomeData`

* add a similar annotation (think about the annotation keyword to use) to a new method `public void logAfterGetAwesomeData()`
* see when logging of this method apears in the console

We can use these two methods to measure how long a method executes. Just start a timer in the `Before` and stop it in the `After`. Try it out!


## Step 5

This all sounds a bit complicated. Is there an easier way of doing this ?
Yes there is.

* create `public Object logAroundGetAwesomeData()`
* annotate it as the before and after (think about the annotation keyword to use)
* add `ProceedingJoinPoint joinPoint` as a parameter
* the before and after functionality is split by the line `joinPoint.proceed();`
	* store the result of this on an `Object result` and return this at the end of the function
	* think about why it throws a Throwable ?
	* think about what `result` contains ?
* implement the same time logging as you did with the two seperate functions at the end of Step 4 (and uncomment them to prevent log diarrhea ;) )

## Step 6

* Proxy
* call method on proxy instead of this in business logic

---
##### ALDA project
The netbeans project for Mr. van Odenhoven can be found [here](https://www.fontysvenlo.org/svnp/2310309/sortingPerformance).
