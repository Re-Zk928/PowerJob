package tech.powerjob.samples.FileRead;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.sdk.MapReduceProcessor;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskResult;

import java.util.List;
@Slf4j
@Component(value = "FinalizeLineJob")
public class FinalizeLineJob implements MapReduceProcessor {

    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        // 这里不需要处理，每个子任务的逻辑在前面完成了
        return new ProcessResult(true, "pass-through");
    }

    @Override
    public ProcessResult reduce(TaskContext context, List<TaskResult> taskResults) {
        for (TaskResult result : taskResults) {
            if (result.isSuccess()) {
                System.out.println("Final result: " + result.getResult());
            }
        }
        return new ProcessResult(true, "Finalized " + taskResults.size() + " results.");
    }
}
