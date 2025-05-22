package tech.powerjob.samples.FileRead;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.sdk.MapReduceProcessor;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.*;
import java.util.*;
@Slf4j
@Component(value = "ReadFileJob")
public class ReadFileJob implements MapReduceProcessor {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        // 从类路径获取资源 input.txt
        ClassLoader classLoader = getClass().getClassLoader();
        Path path = Paths.get(Objects.requireNonNull(classLoader.getResource("tech/powerjob/samples/FileRead/input.txt")).toURI());

        List<String> lines = Files.readAllLines(path);
        return new ProcessResult(true, MAPPER.writeValueAsString(lines));
    }

    @Override
    public ProcessResult reduce(TaskContext context, List<TaskResult> taskResults) {
        // 如果你不需要reduce，可以简单返回
        return new ProcessResult(true, "No reduce logic needed for ReadFileJob.");
    }
}
