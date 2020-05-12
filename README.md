# flowable-triggerable-custom-service-task

In this tutorial, we will be implementing a triggerable custom service task in Flowable. A triggerable task, is one that when it is reached, it is executes its business logic, but once done, it enters a wait state. In order to leave this state, it must be triggered.

## Contribute Code

If you would like to become an active contributor to this project please follow these simple steps:

1.  Fork it
2.  Create your feature branch
3.  Commit your changes
4.  Push to the branch
5.  Create new Pull Request

## What you’ll need

-   About 40 minutes
-   A favorite IDE. In this post, we use [Intellij Community](https://www.jetbrains.com/idea/download/index.html)
-   [JDK 7](http://www.oracle.com/technetwork/java/javase/downloads/index.html) or later. It can be made to work with JDK6, but it will need configuration tweaks. Please check the Spring Boot documentation

## Starting with Spring Initializr

For all Spring applications, it is always a good idea to start with the [Spring Initializr](https://start.spring.io/). The Initializr is an excellent option for pulling in all the dependencies you need for an application and does a lot of the setup for you. This example needs only the Spring Web, and H2 Database dependency. The following image shows the Initializr set up for this sample project:

![Spring Initializr](images/initializr.png)

The following listing shows the `pom.xml` file that is created when you choose Maven:

```java
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.2.7.RELEASE</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.canchitodev.example</groupId>
	<artifactId>spring-flowable-integration</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<packaging>war</packaging>

	<name>spring-flowable-integration</name>
	<description>Demo project for Spring Boot using Flowable BPM</description>

	<organization>
		<name>Canchito Development</name>
		<url>http://www.canchito-dev.com</url>
	</organization>

	<issueManagement>
		<system>Canchito Development</system>
		<url>https://github.com/canchito-dev/triggerable-custom-service-tasks-in-flowable /issues</url>
	</issueManagement>

	<url>http://www.canchito-dev.com/public/blog/2020/05/12/triggerable-custom-service-tasks-in-flowable/</url>

	<properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
		<java.version>1.8</java.version>
		<flowable.version>6.5.0</flowable.version>
	</properties>

	<dependencies>
		<!-- Starter for building web, including RESTful, applications using Spring MVC. Uses Tomcat as the default embedded container -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<!-- Starter for building web, including RESTful, applications using Spring MVC. Uses Tomcat as the default embedded container -->

		<!-- H2 Database Engine -->
		<dependency>
			<groupId>com.h2database</groupId>
			<artifactId>h2</artifactId>
			<scope>runtime</scope>
		</dependency>
		<!-- H2 Database Engine -->

		<!-- Starter for using Tomcat as the embedded servlet container. Default servlet container starter used by spring-boot-starter-web -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-tomcat</artifactId>
		</dependency>
		<!-- Starter for using Tomcat as the embedded servlet container. Default servlet container starter used by spring-boot-starter-web -->

		<!-- Starter for testing Spring Boot applications with libraries including JUnit, Hamcrest and Mockito -->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
			<exclusions>
				<exclusion>
					<groupId>org.junit.vintage</groupId>
					<artifactId>junit-vintage-engine</artifactId>
				</exclusion>
			</exclusions>
		</dependency>
		<!-- Starter for testing Spring Boot applications with libraries including JUnit, Hamcrest and Mockito -->

		<!-- Flowable Spring Boot Starter Basic -->
		<dependency>
			<groupId>org.flowable</groupId>
			<artifactId>flowable-spring-boot-starter-basic</artifactId>
			<version>${flowable.version}</version>
		</dependency>
		<!-- Flowable Spring Boot Starter Basic -->
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>
</project>
```

## Introducing the Triggerable Custom Service Task

It is very common to interact with other services or applications by sending a Kafka message or making a HTTP request for instance. During this interaction, once the process instance has executed the delegate's business logic, it should go into a wait state. At some moment in the future, the external service will return a response and the process instance continues to the next activity.

In the default BPMN notation, this is modeled as a service task followed by a receive task. Nevertheless, this introduces some racing conditions if for instance the external service responses too fast and the process instance has not persisted and the receive task is not active.

In order to solve this, Flowable has come up with a custom attribute (_flowable:triggerable_) available for use on service tasks. What is does is simple. It joins the behavior of a service task together with a receive task. This means that when the process instance reaches this service task, it will execute the logic found in the _method(DelegateExecution)_ function, as it normally does, and then waits for an external trigger before it continues to the next activity. If the async attribute is also set to true for a triggerable service task, the process instance state is first persisted and then the service task logic will be executed in an async job. 

```xml
<serviceTask id="aTriggerableServiceTask" flowable:expression="#{myService.doSomething()}" flowable:triggerable="true" flowable:async="true" />
```

To avoid optimistic lock exceptions, it is recommended to trigger it asynchronously. By default, an asynchronous job is exclusive, meaning that the process instance will be locked. This guarantees that no other activity on the process instance interfere with the trigger logic.