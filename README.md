# Performance testing workshop
Ever wondered how fast your code actually is or how "good" a certain algorithm solves a problem compared to an other.

> "Trust is good, but control is better." (Vladimir Lenin)

Measuring the actual performance of code can be difficult to do. In addition, more often than not, it required changes to the business logic to enable the measuring in the first place.

## The Problem
I want to measure how often a function is called or how long a method call takes.
**But:** I don't want to modify my existing business logic. This problem is called "Cross-cutting concern".

## Scenario
Let's think about a scenario here. You have an existing webservice that does some heavy computations. As more and more users access this service, the responses get slow. You want to find out what is taking so long.

**Solution: Logging**

## Step 1
Lets assume we have this webservice.

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
Create a maven project with these classes in whatever editor you like and run the main **several times**. Notice that the total time it takes to run can vary from 0 to ~ 4 seconds.

## Step 2
Now try to find out why sometimes it takes up to 3 times as long. These lines could help.

```java
	private int expensiveMethodCounter;
	expensiveMethodCounter++;
	System.out.println("Expensive method ran " + expensiveMethodCounter + " times.");
```

We now identified that there is some code which is called quite often sometimes and can take a long time.

However, we modified the actual source code of the implementation in order to allow this.
**-> Not desired**

## Aspect oriented programming (AOP)
Aspect oriented programming is a mechanism of separating cross-cutting concerns. It allows to add additional behavior to existing code, without modifying the code itself.


## Step 3
Now, because we have created a Maven project in Step 1, we can make use of some magic. (Not actual magic, but Maven)

* Delete the three lines added in Step 2
* Add the following Maven dependencies to the POM file:

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

* Build project with dependencies
* Create a folder called `resources` in `<projectRoot>/src/main/`
* Create an XML file `context.xml` in this folder
* Give it this content:

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
    <bean id="loggingAspect" class="logging.LoggingAspect"/>
</beans>
```

* Replace `packagename` with the name of your actual package
* Create a package called `logging` in `Source packages`
* Create a class called `LoggingAspect` in the `logging` package

Now you have the basic setup to start using AOP.

## Step 4
Now we will make use of the spring AOP framework that we just added via Maven.

##### Main class
* Add `ApplicationContext appContext = new ClassPathXmlApplicationContext("context.xml");` as the first line in the main method
	* Replace example with the name of your XML file
* Now we have to get the bean that knows about AOP and use it as our webservice
* Replace the constructor call of `AwesomeWebService` with `appContext.getBean("awesomeWebService");` and cast the result to an `AwesomeWebService`


##### LoggingAspect class
* Import these packages in `LoggingAspect`.

```java
import org.aspectj.lang.annotation.*;
```

* Annotate the class with `@Aspect`
* Create `public void logBeforeGetAwesomeData()`
* Annotate this method with `@Before("execution(* packagename.AwesomeWebService.getAwesomeData(..))")`
	* Replace package name with the name of your actual package
* Now you can write some logging in the `logBeforeGetAwesomeData ` method
* It should appear in the console before the result of `AwesomeWebService.getAwesomeData`

* Add a similar annotation (think about the annotation keyword to use) to a new method `public void logAfterGetAwesomeData()`
* See when logging of this method appears in the console

We can use these two methods to measure how long a method executes. Just start a timer in the `Before` and stop it in the `After`. Try it out!


## Step 5
This all sounds a bit complicated. Is there an easier way of doing this?
Yes there is.

* Create `public Object logAroundGetAwesomeData() throws Throwable`
* Annotate it as the before and after (think about the annotation keyword to use)
* Add `ProceedingJoinPoint joinPoint` as a parameter
* The before and after functionality is split by the line `joinPoint.proceed();`
	* Store the result of this on an `Object result` and return this at the end of the function
	* Think about why it throws a `Throwable`?
  * Catch the `Throwable`, make sure your business logic continues (stop timers, log message) and then rethrow the exception
	* Think about what `result` contains?
* Implement the same time-logging as you did with the two separate functions at the end of Step 4 (and uncomment them to prevent log diarrhea &#9786; )

## Step 6 (Advanced)
This is fine for measuring and testing one method. It also allows for more sophisticated things like measuring how often a method has been executed. But there is one limitation to this.

Let's assume we extend our webservice with a method that does some more elaborate calculations and fetches more data, etc. It could look something like this:

```java
public void magicMethod() {
  //magical things happen here
  try {
    Thread.sleep(random.nextInt(10) + 5);
  } catch (InterruptedException ex) {
  }
}
```

Which we would use like this in our `getAwesomeData`

```java
for (int i = 0; i < runs; i++) {
  //Simulate expensive method
  magicMethod();
}
```

If we now want to also log and measure the `magicMethod`, we will run into some problems. In the `main` method, we defined our bean which is aware of the AOP and call the `getAwesomeData` method on this bean. This bean acts as a proxy to our actual `AwesomeWebService` object and allows the magic of AOP to happen.

Calling a method as `magicMethod();` is logically the same as calling it as `this.magicMethod()`. Because this is the case, we can not easily use AOP on a method that is called inside another method. This is because the proxy bean is not the same as `this` inside the `AwesomeWebService`.

So in order to allow AOP to work with methods that are called by other methods, we need to delegate the method call to the proxy as well. Not nice because strictly speaking, we now modify the business logic. But at least the code is now not being polluted as much as with the earlier solution.

In order to use this, we need to add a proxy to the `AwesomeWebService`.

```java
private AwesomeWebService proxy = this;
```

The next step is to replace the virtual `this` with the proxy object when calling functions.

```java
for (int i = 0; i < runs; i++) {
  //Simulate expensive method
  proxy.magicMethod();
}
```

The last step is to set the proxy Object in the `main` method.

```java
webService.setProxy(webService);
```
Now we can add methods for `before`, `after`, etc. of `magicMethod` in the `LoggingAspect`.
The only thing we need to change is the parameter of the annotation to use the new method instead.

**Task:** Build a mechanism for counting how often `magicMethod` has been executed.

---

#### Limitations
> If your interception needs include protected/private methods or even constructors, consider the use of Spring-driven native AspectJ weaving instead of Spring's proxy-based AOP framework. This constitutes a different mode of AOP usage with different characteristics, so be sure to make yourself familiar with weaving first before making a decision.

This means unfortunately private methods are not supported with the Spring framework and more sophisticated means are required.

---
##### ALDA project
The NetBeans project for Mr. van Odenhoven can be found [here](https://www.fontysvenlo.org/svnp/2310309/sortingPerformance).
