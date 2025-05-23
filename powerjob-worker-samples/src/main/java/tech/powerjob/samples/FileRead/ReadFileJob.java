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
        URL resource = classLoader.getResource("input.txt");  // 直接用文件名，不带包路径

        if (resource == null) {
            throw new RuntimeException("资源文件 input.txt 未找到！");
        }

        Path path = Paths.get(resource.toURI());
        List<String> lines = Files.readAllLines(path);
        return new ProcessResult(true, MAPPER.writeValueAsString(lines));
    }

    @Override
    public ProcessResult reduce(TaskContext context, List<TaskResult> taskResults) {
        // 如果你不需要reduce，可以简单返回
        return new ProcessResult(true, "No reduce logic needed for ReadFileJob.");
    }
}
