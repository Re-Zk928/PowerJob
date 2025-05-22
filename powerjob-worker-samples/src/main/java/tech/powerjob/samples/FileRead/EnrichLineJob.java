package tech.powerjob.samples.FileRead;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.TaskResult;
import tech.powerjob.worker.core.processor.sdk.MapReduceProcessor;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.ProcessResult;

import java.util.List;
@Slf4j
@Component(value = "EnrichLineJob")
public class EnrichLineJob implements MapReduceProcessor {

    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        String processedLine = (String) context.getSubTask();
        String enriched = "[ENRICHED] " + processedLine;
        return new ProcessResult(true, enriched);
    }

    @Override
    public ProcessResult reduce(TaskContext context, List<TaskResult> taskResults) {
        return null;
    }
}
