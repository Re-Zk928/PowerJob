package tech.powerjob.samples.BankWork;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Function;

import org.apache.commons.io.IOUtils; // 用于读取JSON文件
import java.nio.charset.StandardCharsets;

@Component(value = "task_structure_convert")
public class ConvertStructure implements BasicProcessor {

    private static final List<String> TARGET_FIELDS = Arrays.asList(
            "日期", "金额", "机构", "科目", "币种", "交易码", "来源系统", "类型"
    );

    private Map<String, String> loadFieldMapping() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("field-mapping.json");
        if (inputStream == null) {
            throw new FileNotFoundException("映射文件未找到！");
        }
        String json = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, new TypeReference<Map<String, String>>() {});
    }

    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        OmsLogger omsLogger = context.getOmsLogger();
        ObjectMapper objectMapper = new ObjectMapper();
        omsLogger.info("结构化任务开始...");

        // 模拟原始数据：多行自然语言字符串
//        List<String> rawTextList = Arrays.asList(
//                "日期：2025年5月10号，金额200，机构为3 科目是60010101，币种是01 交易码是0，来源系统是0，类型是利息收入",
//                "日期：2025年5月12日，金额:450， 科目:60010102 交易码0，来源系统：1，类型为国债",
//                "日期：2025/05/16， 科目为60010102 来源系统是1， 交易码是01，金额为6660, 类型国债"
//        );

        String rawTextstring = context.getWorkflowContext().fetchWorkflowContext().get("unstructured_list");
        List<String> rawTextList = objectMapper.readValue(
                rawTextstring,
                new TypeReference<List<String>>() {}
        );

        Map<String, String> fieldMapping = loadFieldMapping();
        // 存储所有行的结构化结果
        List<Map<String, Object>> convertedlist = new ArrayList<>();

        for (String rawText : rawTextList) {
            Map<String, Object> structuredRow = new LinkedHashMap<>();
            String rawTextStr = rawText.toString();

            // 动态提取其余字段
            for (Map.Entry<String, String> entry : fieldMapping.entrySet()) {
                String rawField = entry.getKey();
                String targetField = entry.getValue();

                if (targetField.equals("金额")) {
                    String val = extract(rawTextStr, rawField + "[是为:：=]?\\s*([0-9]+(?:\\.[0-9]+)?)", m -> m.group(1));
                    if (val != null) structuredRow.put(targetField, val);
                } else if (targetField.equals("日期")) {
                    // 提取日期，特殊处理
                    structuredRow.put(targetField, parseDate(rawTextStr));
                } else {
                    String val = extract(rawTextStr, rawField + "[是为:：=]?\\s*([\\u4e00-\\u9fa5A-Za-z0-9]+)", m -> m.group(1));
                    if (val != null) structuredRow.put(targetField, val);
                }
            }

            convertedlist.add(structuredRow);
            omsLogger.info("单行解析结果: {}", structuredRow);
        }

        // 将结果放入工作流上下文
        context.getWorkflowContext().appendData2WfContext("convertedlist", convertedlist);

        return new ProcessResult(true, "结构化成功，结果为： " + convertedlist);
    }

    private String extract(String text, String pattern, Function<Matcher, String> extractor) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(text);
        return m.find() ? extractor.apply(m) : null;
    }

    private String parseDate(String text) {
        // 匹配 2025-05-18 或 2025-05-18T08:54:51.326803Z
        Pattern isoPattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})(?:[T\\s]\\d{2}:\\d{2}:\\d{2}(?:\\.\\d+)?Z?)?");
        Matcher matcher = isoPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1); // 只提取前半段日期部分
        }
        return null;
    }
}
