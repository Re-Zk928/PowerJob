package tech.powerjob.samples.BankWork;
import com.fasterxml.jackson.core.type.TypeReference;
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
        OmsLogger omsLogger = context.getOmsLogger();
        ObjectMapper objectMapper = new ObjectMapper();

        // 从上下文中获取 other_bank_list 字符串
        String jsonStr = (String) context.getWorkflowContext().fetchWorkflowContext().get("other_bank_list");

        List<Object> otherBankList = objectMapper.readValue(jsonStr, new TypeReference<List<Object>>() {
        });

        List<Map<String, Object>> structuredList = new ArrayList<>();
        List<String> unstructuredList = new ArrayList<>();

        for (Object item : otherBankList) {
            if (item instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) item;
                if (map.containsKey("raw")) {
                    unstructuredList.add((String) map.get("raw"));
                } else {
                    structuredList.add(map);
                }
            } else if (item instanceof String) {
                unstructuredList.add((String) item);
            }
        }

        // 保存结果
        context.getWorkflowContext().appendData2WfContext("structured_list", structuredList);
        context.getWorkflowContext().appendData2WfContext("unstructured_list", unstructuredList);

        omsLogger.info("结构化记录: " + structuredList.size() + " 条，非结构化记录: " + unstructuredList.size() + " 条");

        return new ProcessResult(true, "task_data_split 执行成功");
    }
}
