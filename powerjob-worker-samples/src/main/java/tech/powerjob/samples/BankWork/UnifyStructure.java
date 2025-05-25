package tech.powerjob.samples.BankWork;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component(value = "task_unify_structure")
public class UnifyStructure implements BasicProcessor {
    private static final List<String> FIELD_ORDER = Arrays.asList(
            "日期", "金额", "机构", "科目", "币种", "交易码", "来源系统", "类型"
    );

    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        OmsLogger omsLogger = context.getOmsLogger();
        omsLogger.info("统一结构化任务开始，任务上下文: {}", context);

        // 1. 从 WorkflowContext 获取 structuredData
        String convertedList = context.getWorkflowContext().fetchWorkflowContext().get("convertedlist");
        String structuredList = context.getWorkflowContext().fetchWorkflowContext().get("structured_list");

        // 2. 解析 JSON 为 List<Map<String, Object>>
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> rawData1 = new ArrayList<>();
        List<Map<String, Object>> rawData2 = new ArrayList<>();
        try {
            rawData1 = mapper.readValue(convertedList, new TypeReference<List<Map<String, Object>>>() {
            });
            rawData2 = mapper.readValue(structuredList, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (Exception e) {
            omsLogger.error("JSON 解析失败: {}", e.getMessage());
            return new ProcessResult(false, "JSON 格式错误");
        }

        rawData1.addAll(rawData2);
        List<Map<String, Object>> structureddata = rawData1;

        if (structureddata == null) {
            omsLogger.error("未找到名为 structuredData 的工作流上下文数据！");
            return new ProcessResult(false, "structuredData 缺失");
        }

        // 3. 字段统一化与排序
        List<Map<String, Object>> unifiedList = new ArrayList<>();
        for (Map<String, Object> record : structureddata) {
            Map<String, Object> unifiedRecord = new LinkedHashMap<>();

            for (String field : FIELD_ORDER) {
                Object value = record.get(field);

                switch (field) {
                    case "日期":
                        unifiedRecord.put("日期", parseDate(value));
                        break;
                    case "金额":
                        unifiedRecord.put("金额", parseAmount(value));
                        break;
                    case "机构":
                    case "科目":
                    case "币种":
                    case "交易码":
                    case "来源系统":
                        unifiedRecord.put(field, value != null ? value.toString() : null);
                        break;
                    case "类型":
                        unifiedRecord.put("类型", value != null ? value.toString() : "");
                        break;
                }
            }

            unifiedList.add(unifiedRecord);
        }

        // 4. 打印/上传/传递
        omsLogger.info("统一结构化完成，共 {} 条数据，结果如下：{}", unifiedList.size(), unifiedList);
        // 转换为JSON格式输出
        String unifiedjsonList = JSON.toJSONString(unifiedList, true);
        // 如果你还需要把数据传给下个节点
        context.getWorkflowContext().appendData2WfContext("unifiedData", unifiedjsonList);
        context.getWorkflowContext().appendData2WfContext("convertedlist", "[]");  // 设为空数组JSON
        context.getWorkflowContext().appendData2WfContext("structured_list", "[]"); // 设为空数组JSON
        return new ProcessResult(true, "统一结构化成功，已处理: " + unifiedjsonList);
    }

    private String parseDate(Object value) {
        if (value == null) return null;
        try {
            return LocalDate.parse(value.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd")).toString();
        } catch (Exception e) {
//            log.warn("日期解析失败: {}", value);
            return null;
        }
    }

    private BigDecimal parseAmount(Object value) {
        if (value == null) return BigDecimal.ZERO;
        try {
            String amountStr = value.toString().replaceAll("[^0-9.\\-]", ""); // 保留负号和小数点
            if (amountStr.isEmpty()) return BigDecimal.ZERO;
            return new BigDecimal(amountStr);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
