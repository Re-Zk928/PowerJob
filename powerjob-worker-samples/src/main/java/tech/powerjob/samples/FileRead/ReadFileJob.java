package tech.powerjob.samples.FileRead;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.sdk.MapReduceProcessor;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URL;
import java.nio.file.*;
import java.util.*;
@Slf4j
@Component(value = "ReadFileJob")
public class ReadFileJob implements MapReduceProcessor {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource("input.txt");
        if (resource == null) {
            throw new RuntimeException("资源文件 input.txt 未找到！");
        }
        Path path = Paths.get(resource.toURI());
        List<String> lines = Files.readAllLines(path);

        // 直接返回需要分割的任务数据
        // PowerJob框架会自动处理任务分割
        return new ProcessResult(true, MAPPER.writeValueAsString(lines));
    }

    @Override
    public ProcessResult reduce(TaskContext context, List<TaskResult> taskResults) {
        // 在这里处理所有子任务的结果
        log.info("收到所有子任务结果: {}", taskResults);
        return new ProcessResult(true, "ReadFileJob reduce finish.");
    }
}
