package tech.powerjob.samples.FileRead;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.TaskResult;
import tech.powerjob.worker.core.processor.sdk.MapReduceProcessor;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.ProcessResult;

import java.util.List;
@Slf4j
@Component(value = "ProcessLineJob")
public class ProcessLineJob implements MapReduceProcessor {

    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        String line = (String) context.getSubTask(); // 每个任务只拿一行
        return new ProcessResult(true, line.toUpperCase()); // 转大写
    }

    @Override
    public ProcessResult reduce(TaskContext context, List<TaskResult> taskResults) {
        return null;
    }
}
