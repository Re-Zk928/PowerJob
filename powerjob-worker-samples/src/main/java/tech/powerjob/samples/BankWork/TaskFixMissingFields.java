package tech.powerjob.samples.BankWork;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;


@Slf4j
@Component(value = "task_fix_missing_fields")
public class TaskFixMissingFields implements BasicProcessor {
    public ProcessResult process(TaskContext context) throws Exception {
        return null;
    }
}