package tech.powerjob.samples.testFlow;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component(value = "First")
public class FirstClass implements BasicProcessor {

    @Override
    public ProcessResult process(TaskContext context) throws Exception {

        OmsLogger omsLogger = context.getOmsLogger();
        omsLogger.info("FirstTask 执行");

        // 模拟要传递的 bank_data 列表
        List<Map<String, Object>> bankData = new ArrayList<>();
        Map<String, Object> record = new HashMap<>();
        record.put("机构", "银行A");
        record.put("科目", "存款");
        record.put("币种", "CNY");
        record.put("交易码", "60010101");
        record.put("交易日期", "2025-05-16");
        record.put("金额", 1000000);
        record.put("来源系统", "系统A");
        bankData.add(record);

        // 转 JSON 并放入工作流上下文，key 为 "bank_data"
        String json = tech.powerjob.common.serialize.JsonUtils.toJSONString(bankData);
        context.getWorkflowContext().appendData2WfContext("bank_data", json);

        return new ProcessResult(true, "First 任务执行完成");
    }
}
