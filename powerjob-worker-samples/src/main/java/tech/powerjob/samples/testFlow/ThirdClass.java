package tech.powerjob.samples.testFlow;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.util.Map;

@Slf4j
@Component(value = "Third")
public class ThirdClass implements BasicProcessor {

    @Override
    public ProcessResult process(TaskContext context) throws Exception {

        // PowerJob 在线日志功能，使用该 Logger 打印的日志可以直接在 PowerJob 控制台查看
        OmsLogger omsLogger = context.getOmsLogger();
        Map<String, String> wfContext = context.getWorkflowContext().fetchWorkflowContext();
        String dataFromFirst = wfContext.get("dataFromFirst");
        context.getOmsLogger().info("Third 获取到数据：{}", dataFromFirst);
        return new ProcessResult(true,"Third 任务执行完成，收到数据：" + dataFromFirst );

    }
}
