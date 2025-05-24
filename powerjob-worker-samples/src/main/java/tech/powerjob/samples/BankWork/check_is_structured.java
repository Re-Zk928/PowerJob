package tech.powerjob.samples.BankWork;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.core.processor.sdk.MapReduceProcessor;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import tech.powerjob.worker.log.OmsLogger;

import java.net.URL;
import java.nio.file.*;
import java.util.*;
@Slf4j
@Component(value = "check_is_structured")
public class check_is_structured implements BasicProcessor {
    @Override
    public ProcessResult process(TaskContext context) throws Exception {

        // PowerJob 在线日志功能，使用该 Logger 打印的日志可以直接在 PowerJob 控制台查看
        OmsLogger omsLogger = context.getOmsLogger();
        context.getOmsLogger().info("FirstTask 执行");
        context.getWorkflowContext().appendData2WfContext("dataFromFirst", "这是来自第一个任务的数据");
        return new ProcessResult(true, "First 任务执行完成");

    }
}
