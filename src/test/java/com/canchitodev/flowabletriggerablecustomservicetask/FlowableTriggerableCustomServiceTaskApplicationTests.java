/**
 * This content is released under the MIT License (MIT)
 *
 * Copyright (c) 2020, canchito-dev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author 		José Carlos Mendoza Prego
 * @copyright	Copyright (c) 2020, canchito-dev (http://www.canchito-dev.com)
 * @license		http://opensource.org/licenses/MIT	MIT License
 * @link		http://www.canchito-dev.com/public/blog/2020/05/12/flowable-triggerable-custom-service-task/
 * @link		https://github.com/canchito-dev/flowable-triggerable-custom-service-task
 **/
package com.canchitodev.flowabletriggerablecustomservicetask;

import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.flowable.task.api.Task;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

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