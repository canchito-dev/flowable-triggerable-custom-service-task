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

package com.canchitodev.flowabletriggerablecustomservicetask.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.delegate.TriggerableActivityBehavior;

import java.io.Serializable;

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