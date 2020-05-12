package com.canchitodev.flowabletriggerablecustomservicetask;

import org.flowable.engine.RuntimeService;
import org.flowable.engine.test.Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(classes = FlowableTriggerableCustomServiceTaskApplication.class)
@DirtiesContext
class FlowableTriggerableCustomServiceTaskApplicationTests {

	@Autowired
	private RuntimeService runtimeService;

	@Deployment(resources = "processes/triggerable-custom-service-task.bpmn20.bpmn")
	void contextLoads() {}
}