package tech.powerjob.samples.BankWork;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.powerjob.common.serialize.JsonUtils;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.log.OmsLogger;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.apache.commons.io.IOUtils;
@Slf4j
@Component(value = "task_data_classify")
public class task_data_classify implements BasicProcessor {


    public Map loadFieldMapping() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("field-mapping.json");
        if (inputStream == null) {
            throw new FileNotFoundException("映射文件未找到！");
        }
        String json = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        return JsonUtils.parseObject(json, Map.class);
    }


    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        OmsLogger omsLogger = context.getOmsLogger();
        List<Map<String, Object>> bankAList = new ArrayList<>();
        List<Map<String, Object>> otherBankList = new ArrayList<>();
        String isBankA = "";
        String isOtherBank = "";

        String jsonStr = context.getWorkflowContext().fetchWorkflowContext().get("bank_data");

        try {
            // 解析 jsonStr 为 JsonNode（支持数组或错误结构）
            JsonNode rootNode = new ObjectMapper().readTree(jsonStr);

            if (rootNode.isArray()) {
                for (JsonNode node : rootNode) {
                    if (node.isObject()) {
                        // 是结构化 JSON 对象
                        Map<String, Object> map = new ObjectMapper().convertValue(node, new TypeReference<Map<String, Object>>() {});
                        String allValues = map.values().toString();
                        if (allValues.contains("银行A")) {
                            bankAList.add(map);
                            isBankA = "1";
                        } else {
                            otherBankList.add(map);
                        }
                    } else if (node.isTextual()) {
                        // 是纯文本记录
                        String line = node.asText();
                        Map<String, Object> map = new HashMap<>();
                        map.put("raw", line);
                        otherBankList.add(map);
                        isOtherBank = "1";

                    }
                }
            } else {
                omsLogger.warn("jsonStr 不是数组格式");
            }
        } catch (Exception e) {
            omsLogger.error("解析 jsonStr 出错", e);
        }

        // 保存处理结果回上下文
        context.getWorkflowContext().appendData2WfContext("bank_a_list", bankAList);
        context.getWorkflowContext().appendData2WfContext("other_bank_list", otherBankList);
        context.getWorkflowContext().appendData2WfContext("is_bank_a", isBankA);
        context.getWorkflowContext().appendData2WfContext("is_other_bank", isOtherBank);
        omsLogger.info("提取包含银行A的记录完成，共计：" + bankAList.size() + " 条");
        return new ProcessResult(true, "task_data_classify 任务执行完成");
    }

}
