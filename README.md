[![Buy Me a Coffee](images/donate_with_crypto.PNG)](https://commerce.coinbase.com/checkout/faf64f90-2e80-46ee-aeba-0fde14cbeb46)
[![Buy Me a Coffee](https://www.paypalobjects.com/en_US/ES/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/donate?hosted_button_id=GTSXAJQEBZ7XG)

# flowable-triggerable-custom-service-task

In this tutorial, we will be implementing a triggerable custom service task in Flowable. A triggerable task, is one that when it is reached, it is executes its business logic, but once done, it enters a wait state. In order to leave this state, it must be triggered.

## License
The MIT License (MIT)  

Copyright (c) 2020, canchito-dev  

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:  

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.  

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

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

```xml
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
<serviceTask id="aTriggerableServiceTask" 
    flowable:expression="#{myService.doSomething()}" 
    flowable:triggerable="true" 
    flowable:async="true" 
/>
```

To avoid optimistic lock exceptions, it is recommended to trigger it asynchronously. By default, an asynchronous job is exclusive, meaning that the process instance will be locked. This guarantees that no other activity on the process instance interfere with the trigger logic.

## Creating a Triggerable Custom Service Task

The first thing you need to do, is create a class and call it TriggerableServiceTask. This class will implement JavaDelegate, TriggerableActivityBehavior and Serializable. Once you have done this, you will need to override the methods execute and trigger. Here is the example code:

```java
@Service("triggerableServiceTask")
@Scope("prototype")
public class TriggerableServiceTask implements JavaDelegate, TriggerableActivityBehavior, Serializable {

    @Override
    public void execute(DelegateExecution execution) {
        incrementCount(execution);
    }

    @Override
    public void trigger(DelegateExecution execution, String signalName, Object signalData) {
        incrementCount(execution);
    }

    public void incrementCount(DelegateExecution execution) {
        String variableName = "count";
        int count = 0;
        if (execution.hasVariable(variableName)) {
            count = (int) execution.getVariable(variableName);
        }
        count++;
        execution.setVariable(variableName, count);
    }
}
```

The logic is simple. When the _execute_ method is reached, it calls the _incrementCount_, which it creates a process variable (if it does not exists) named "count", increases its current value by one, and updates it so that it is available during the process execution.

When the _trigger_ method is reached, the same logic as with the _execute_ method is followed.

## Testing the Task

To test it, create a file named _triggerable-custom-service-task.bpmn20.xml_ inside the folder `src/main/resources/processes`. This is a dummy process definition. The content of the file is below:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions
        xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
        xmlns:flowable="http://flowable.org/bpmn"
        targetNamespace="Examples">
    <process id="triggerableCustomServiceTask" name="Triggerable Custom Service Task">
        <startEvent id="theStart" />
        <sequenceFlow sourceRef="theStart" targetRef="service1" />
        <serviceTask id="service1" flowable:delegateExpression="${triggerableServiceTask}" flowable:async="true" flowable:triggerable="true"/>
        <sequenceFlow sourceRef="service1" targetRef="usertask1" />
        <userTask id="usertask1" name="Task A"/>
        <sequenceFlow sourceRef="usertask1" targetRef="theEnd" />
        <endEvent id="theEnd" />
    </process>
</definitions>
```

For testing the workflow, we will use Flowable's API. But first, we need to add jUnit dependencies. Open the `pom.xml file and add:

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>${junit.jupiter.version}</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>junit</groupId>
    <artifactId>junit</artifactId>
    <version>${junit.version}</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-engine</artifactId>
    <version>${junit.jupiter.version}</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.junit.vintage</groupId>
    <artifactId>junit-vintage-engine</artifactId>
    <version>${junit.vintage.version}</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.awaitility</groupId>
    <artifactId>awaitility</artifactId>
</dependency>
```

And under the <build> section, add the following plugins:

```xml
<plugin>
	<artifactId>maven-surefire-plugin</artifactId>
	<version>2.22.2</version>
</plugin>

<plugin>
	<artifactId>maven-failsafe-plugin</artifactId>
	<version>2.22.2</version>
</plugin>
```

Afterwards, create a testing class under folder `src/main/test/java. In our case, we named the class `FlowableTriggerableCustomServiceTaskApplicationTests. And here is the code:

```java
@SpringBootTest
class FlowableTriggerableCustomServiceTaskApplicationTests {

	@Autowired
	private RuntimeService runtimeService;

	@Autowired
	private TaskService taskService;

	@Autowired
	private HistoryService historyService;

	@Test
	@Deployment(resources = "processes/triggerable-custom-service-task.bpmn20.bpmn")
	void testTriggerableCustomServiceTask() {
		// Start a new process instance
		ProcessInstance processInstance = this.runtimeService.startProcessInstanceByKey("triggerableCustomServiceTask");

		// Check if triggarable custom service task was reached
		await().atMost(30L, TimeUnit.SECONDS).until(
				() -> this.runtimeService.createExecutionQuery()
						.activityId("service1")
						.processInstanceId(processInstance.getProcessInstanceId())
						.singleResult() != null
		);

		// Get the value of the variable 'count' before the trigger
		HistoricVariableInstance historicVariableInstance = this.historyService.createHistoricVariableInstanceQuery()
				.processInstanceId(processInstance.getProcessInstanceId())
				.variableName("count")
				.singleResult();

		// Let's see the value of 'count' as it was modified in the execute method
		System.out.println(String.format("Before trigger: %s", historicVariableInstance.getValue()));

		// We need the execution Id of the triggerable service task
		Execution execution = this.runtimeService.createExecutionQuery()
				.processInstanceId(processInstance.getProcessInstanceId())
				.activityId("service1")
				.singleResult();

		// Trigger the service task.
		this.runtimeService.trigger(execution.getId());

		// Get the value of the variable 'count' after the trigger
		historicVariableInstance = this.historyService.createHistoricVariableInstanceQuery()
				.processInstanceId(processInstance.getProcessInstanceId())
				.variableName("count")
				.singleResult();

		// Let's see the value of 'count' as it was modified in the trigger method
		System.out.println(String.format("After trigger: %s", historicVariableInstance.getValue()));

		// Check if the user task was reached
		await().atMost(30L, TimeUnit.SECONDS).until(
				() -> this.runtimeService.createExecutionQuery()
						.activityId("usertask1")
						.processInstanceId(processInstance.getProcessInstanceId())
						.singleResult() != null
		);

		// Get the task from the TaskService
		Task task = this.taskService.createTaskQuery()
				.processInstanceId(processInstance.getProcessInstanceId())
				.taskName("Task A")
				.singleResult();

		// Complete the user task
		this.taskService.complete(task.getId());

		// Make sure the process has ended
		await().atMost(30L, TimeUnit.SECONDS).until(
				() -> this.historyService.createHistoricProcessInstanceQuery()
						.processInstanceId(processInstance.getProcessInstanceId())
						.finished()
						.singleResult() != null
		);
	}
}
```

The test is very simple. Here are the steps that we performed. They are also included as comments in the sample code:

1. Start a process instance
2. Check is the triggerable custom service task was reached
3. Get the value of the variable 'count' before the trigger
4. Let's see the value of 'count' as it was modified in the execute method, by printing it in the log console
5. Get  the execution Id of the triggerable custom service task
6. Trigger the service task
7. Get the value of the variable 'count' after the trigger
8. Let's see the value of 'count' as it was modified in the trigger method, by printing it in the log console
9. Check if the user task was reached
10. Get the task from the TaskService
11. Complete the user task
12. Make sure the process has ended

If you execute the test, you should see these lines logged:

```
Before trigger: 1 
After trigger: 2
```

## Summary

In this post, we have shown how to create a custom and triggerable service task in Flowable. We hope that, even though this was a very basic introduction, you understood how to use and configure them. We will try to go deeper into Flowable in upcoming posts.

Please feel free to contact us. We will gladly response to any doubt or question you might have.

Source code can be found in our [GitHub](https://github.com/canchito-dev/flowable-triggerable-custom-service-task) repository.
