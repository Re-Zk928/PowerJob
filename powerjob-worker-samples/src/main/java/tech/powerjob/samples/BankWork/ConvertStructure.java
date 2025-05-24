package tech.powerjob.samples.BankWork;

import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Function;

@Component(value = "task_structure_convert")
public class ConvertStructure implements BasicProcessor {

    private static final List<String> TARGET_FIELDS = Arrays.asList(
            "日期", "金额", "机构", "科目", "币种", "交易码", "来源系统", "类型"
    );

    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        OmsLogger omsLogger = context.getOmsLogger();
        omsLogger.info("结构化任务开始...");

        // 模拟原始数据：多行自然语言字符串
        List<String> rawTextList = Arrays.asList(
                "日期：2025年5月10号，金额200，机构为3 科目是60010101，币种是01 交易码是0，来源系统是0，类型是利息收入",
                "日期：2025年5月12日，金额:450， 科目:60010102 交易码0，来源系统：1，类型为国债",
                "日期：2025/05/16， 科目为60010102 来源系统是1， 交易码是01，金额为6660, 类型国债"
        );

        // 存储所有行的结构化结果
        List<Map<String, Object>> structuredDataList = new ArrayList<>();

        // 处理每一行数据
        for (String rawText : rawTextList) {
            Map<String, Object> structuredRow = new LinkedHashMap<>();
            // 提取日期
            structuredRow.put("日期", parseDate(extract(rawText,
                    "(?:日期[是为:：]?\\s*)?([0-9年月日号./-]+)",
                    m -> m.group(1))));
            // 提取其他字段
            structuredRow.put("金额", extract(rawText, "金额[是为:：]?\\s*(\\d+)", m -> m.group(1)));
            structuredRow.put("机构", extract(rawText, "机构[是为:：]?\\s*(\\d+)", m -> m.group(1)));
            structuredRow.put("科目", extract(rawText, "科目[是为:：]?\\s*(\\d+)", m -> m.group(1)));
            structuredRow.put("币种", extract(rawText, "币种[是为:：]?\\s*(\\d+)", m -> m.group(1)));
            structuredRow.put("交易码", extract(rawText, "交易码[是为:：]?\\s*(\\d+)", m -> m.group(1)));
            structuredRow.put("来源系统", extract(rawText, "来源系统[是为:：]?\\s*(\\d+)", m -> m.group(1)));
            structuredRow.put("类型", extract(rawText, "类型[是为:：]?\\s*([\\u4e00-\\u9fa5\\w]+)", m -> m.group(1)));

            // 添加到结果列表
            structuredDataList.add(structuredRow);
            omsLogger.info("单行解析结果: {}", structuredRow);
        }

        // 转换为JSON格式输出
        String jsonResult = JSON.toJSONString(structuredDataList, true);
        omsLogger.info("完整结构化结果(JSON格式):\n{}", jsonResult);

        // 将结果放入工作流上下文
        context.getWorkflowContext().appendData2WfContext("structuredData", jsonResult);

        return new ProcessResult(true, "结构化成功，结果为： " + jsonResult);
    }

    private String extract(String text, String pattern, Function<Matcher, String> extractor) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(text);
        return m.find() ? extractor.apply(m) : null;
    }
    private String parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        // 统一处理中文日期格式
        dateStr = dateStr.replace("号", "").replace("日", "").trim();

        try {
            // 尝试解析中文格式 "yyyy年M月d"
            Matcher cnMatcher = Pattern.compile("(\\d{4})年(\\d{1,2})月(\\d{1,2})").matcher(dateStr);
            if (cnMatcher.find()) {
                return String.format("%s-%02d-%02d",
                        cnMatcher.group(1),
                        Integer.parseInt(cnMatcher.group(2)),
                        Integer.parseInt(cnMatcher.group(3)));
            }

            // 尝试解析ISO格式 "yyyy-MM-dd"
            if (dateStr.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
                String[] parts = dateStr.split("-");
                return String.format("%s-%02d-%02d",
                        parts[0],
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]));
            }

            // 尝试解析点分隔格式 "yyyy.MM.dd"
            if (dateStr.matches("\\d{4}\\.\\d{1,2}\\.\\d{1,2}")) {
                String[] parts = dateStr.split("\\.");
                return String.format("%s-%02d-%02d",
                        parts[0],
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]));
            }

            // 尝试解析斜杠分隔格式 "yyyy/MM/dd"
            if (dateStr.matches("\\d{4}/\\d{1,2}/\\d{1,2}")) {
                String[] parts = dateStr.split("/");
                return String.format("%s-%02d-%02d",
                        parts[0],
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2]));
            }

            // 尝试解析紧凑格式 "yyyyMMdd"
            if (dateStr.matches("\\d{8}")) {
                return String.format("%s-%s-%s",
                        dateStr.substring(0, 4),
                        dateStr.substring(4, 6),
                        dateStr.substring(6, 8));
            }

            // 尝试使用Java内置解析器
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE).toString();
            } catch (DateTimeParseException e) {
                // 最后尝试灵活解析
                return parseFlexibleDate(dateStr);
            }
        } catch (Exception e) {
//            log.warn("日期解析失败: {}, 使用原始值", dateStr);
            return dateStr; // 返回原始值或根据需求返回null
        }
    }

    // 更灵活的日期解析
    private String parseFlexibleDate(String dateStr) {
        // 提取所有数字
        String digits = dateStr.replaceAll("[^0-9]", "");
        if (digits.length() >= 8) {
            // 假设前8位是年月日
            digits = digits.substring(0, 8);
            return String.format("%s-%s-%s",
                    digits.substring(0, 4),
                    digits.substring(4, 6),
                    digits.substring(6, 8));
        } else if (digits.length() == 6) {
            // 假设前6位是年月(没有日)
            return String.format("%s-%s-01",
                    digits.substring(0, 4),
                    digits.substring(4, 6));
        }
        return dateStr; // 无法解析则返回原始值
    }
}
