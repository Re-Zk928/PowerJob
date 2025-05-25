package tech.powerjob.samples.BankWork;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.util.*;
import java.util.Map.Entry;

@Slf4j
@Component(value = "task_format_standardize")
public class task_format_standardize implements BasicProcessor {

    // ====================== 机构映射规则 ======================
    private static final Map<String, String> ORG_MAPPING;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("银行A", "1");
        map.put("银行B", "2");
        map.put("银行C", "3");
        ORG_MAPPING = Collections.unmodifiableMap(map); // 转为不可变Map（可选）
    }

    // ====================== 科目映射规则 ======================
    private static final Map<String, String> SUBJECT_MAPPING;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("信用卡", "60010101");
        map.put("理财产品", "60010102");
        map.put("定期存款", "60010103");
        map.put("活期存款", "60010104");
        map.put("贷款", "60010105");
        SUBJECT_MAPPING = Collections.unmodifiableMap(map);
    }

    // ====================== 币种映射规则 ======================
    private static final Map<String, String> CURRENCY_MAPPING;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("JPY", "01");
        map.put("EUR", "02");
        map.put("USD", "03");
        map.put("CNY", "04");
        CURRENCY_MAPPING = Collections.unmodifiableMap(map);
    }

    // ====================== 交易码映射规则 ======================
    private static final Map<String, String> TRADE_CODE_MAPPING;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("TX002", "7502");
        map.put("TX004", "7504");
        map.put("TX001", "7501");
        map.put("TX003", "7503");
        TRADE_CODE_MAPPING = Collections.unmodifiableMap(map);
    }

    // ====================== 来源系统映射规则 ======================
    private static final Map<String, String> SYSTEM_MAPPING;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("核心系统", "1");
        map.put("柜面系统", "2");
        map.put("移动银行", "3");
        map.put("网银系统", "4");
        SYSTEM_MAPPING = Collections.unmodifiableMap(map);
    }

    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        OmsLogger omsLogger = context.getOmsLogger();
        ObjectMapper objectMapper = new ObjectMapper();
        omsLogger.info("开始执行格式标准化任务");

        // 1. 从任务上下文中获取前序任务处理后的JSON数据
        String inputJson = (String) context.getWorkflowContext().fetchWorkflowContext().get("fixedData");
        if (inputJson == null || inputJson.isEmpty()) {
            omsLogger.error("未获取到待处理的JSON数据");
            return new ProcessResult(false, "输入数据为空");
        }

        // 2. 解析JSON为List<Map<String, Object>>
        List<Map<String, Object>> dataList;
        try {
            dataList = objectMapper.readValue(inputJson, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            omsLogger.error("JSON解析失败: {}", e.getMessage());
            return new ProcessResult(false, "JSON解析失败");
        }

        // 3. 遍历数据并应用转换规则
        List<Map<String, Object>> standardizedList = new ArrayList<>();
        for (Map<String, Object> record : dataList) {
            Map<String, Object> standardizedRecord = new LinkedHashMap<>(record); // 复制原始记录

            // 处理机构字段
            String org = (String) record.get("机构");
            standardizedRecord.put("机构", ORG_MAPPING.getOrDefault(org, "0"));

            // 处理科目字段
            String subject = (String) record.get("科目");
            standardizedRecord.put("科目", SUBJECT_MAPPING.getOrDefault(subject, "00000000"));

            // 处理币种字段
            String currency = (String) record.get("币种");
            standardizedRecord.put("币种", CURRENCY_MAPPING.getOrDefault(currency, "00"));

            // 处理交易码字段
            String tradeCode = (String) record.get("交易码");
            standardizedRecord.put("交易码", TRADE_CODE_MAPPING.getOrDefault(tradeCode, "0000"));

            // 处理来源系统字段
            String system = (String) record.get("来源系统");
            standardizedRecord.put("来源系统", SYSTEM_MAPPING.getOrDefault(system, "0"));

            // 统一类型字段为"利息收入"
            standardizedRecord.put("类型", "利息收入");

            standardizedList.add(standardizedRecord);
        }

        // 4. 转换为JSON字符串并输出到任务上下文
        String outputJson = JSON.toJSONString(standardizedList, true); // true表示格式化输出
        context.getWorkflowContext().appendData2WfContext("standardizedData", outputJson);
        omsLogger.info("格式标准化完成，输出数据: {}", outputJson);

        return new ProcessResult(true, "格式标准化任务执行成功");
    }
}