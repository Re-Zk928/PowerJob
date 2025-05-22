package tech.powerjob.samples.testFlow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.util.Map;

@Slf4j
@Component(value = "Second")
public class SecondClass implements BasicProcessor {

    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        OmsLogger omsLogger = context.getOmsLogger();

        // Step 1: 追加自定义 key 到上下文
        String key = "value";
        String value = "1";
        context.getWorkflowContext().appendData2WfContext(key, value);
        omsLogger.info("Step1: 成功追加 key=value -> {}={}", key, value);

        // Step 2: 获取第一次 fetch 的上下文快照（可能还没有 value）
        Map<String, String> wfContext1 = context.getWorkflowContext().fetchWorkflowContext();
        omsLogger.info("Step2: 第一次 fetchWorkflowContext 拿到的数据: {}", wfContext1);

        // Step 3: 获取上一个任务传来的 dataFromFirst
        String dataFromFirst = wfContext1.get("dataFromFirst");
        omsLogger.info("Step3: 获取上一个任务传来的数据 dataFromFirst -> {}", dataFromFirst);

        // Step 4: 将处理结果继续追加到上下文
        String result = "Second 任务执行完成，收到数据：" + dataFromFirst;
        context.getWorkflowContext().appendData2WfContext("dataFromFirst", result);
        omsLogger.info("Step4: 追加新数据 dataFromFirst -> {}", result);

        // Step 5: 再次 fetch 看上下文是否有更新（仍可能没有 append 的数据）
        Map<String, String> wfContext2 = context.getWorkflowContext().fetchWorkflowContext();
        omsLogger.info("Step5: 第二次 fetchWorkflowContext 拿到的数据: {}", wfContext2);

        // 最终返回处理结果
        return new ProcessResult(true, result);
    }
}
