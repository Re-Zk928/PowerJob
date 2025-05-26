package tech.powerjob.samples.BankWork;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Component(value = "task_final_output")
public class task_final_output implements BasicProcessor {

    // 定义Excel表头（含序号）
    private static final String[] HEADERS = {
            "序号", "日期", "金额", "交易维度1（机构）", "交易维度2（科目）",
            "交易维度3（货币）", "交易维度4（交易码）", "交易维度5（来源系统）", "交易维度6（类型）"
    };

    // 定义JSON数据中的实际字段映射（与表头对应）
    private static final String[] JSON_FIELDS = {
            null, "日期", "金额", "机构", "科目", "币种", "交易码", "来源系统", "类型"
    };

    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        OmsLogger omsLogger = context.getOmsLogger();
        omsLogger.info("开始执行Excel导出任务");

        // 1. 从任务上下文获取标准化后的JSON数据
        String standardizedJson = (String) context.getWorkflowContext().fetchWorkflowContext().get("standardizedData");
        if (standardizedJson == null || standardizedJson.isEmpty()) {
            omsLogger.error("未获取到待导出的JSON数据");
            return new ProcessResult(false, "standardizedData 数据为空");
        }

        // 2. 解析JSON为List<Map<String, Object>>
        List<Map<String, Object>> dataList = JSON.parseObject(
                standardizedJson,
                new TypeReference<List<Map<String, Object>>>() {}
        );

        // 3. 创建Excel工作簿和工作表
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(getOutputFilePath())) {

            Sheet sheet = workbook.createSheet("银行数据报表");
            createHeaderRow(sheet); // 创建表头
            fillDataRows(sheet, dataList); // 填充数据行

            // 自动调整列宽
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // 写入文件
            workbook.write(fos);
            omsLogger.info("Excel文件已保存至: {}", getOutputFilePath());
        } catch (IOException e) {
            omsLogger.error("Excel导出失败: {}", e.getMessage());
            return new ProcessResult(false, "文件写入失败");
        }

        return new ProcessResult(true, "Excel导出任务执行成功");
    }

    /**
     * 创建表头行
     */
    private void createHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderCellStyle(sheet.getWorkbook());

        for (int i = 0; i < HEADERS.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(HEADERS[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * 填充数据行
     */
    private void fillDataRows(Sheet sheet, List<Map<String, Object>> dataList) {
        int rowNum = 1;
        for (Map<String, Object> record : dataList) {
            Row row = sheet.createRow(rowNum++);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = row.createCell(i);

                // 处理序号列
                if (i == 0) {
                    cell.setCellValue(rowNum - 1); // 序号从1开始
                    continue;
                }

                // 获取JSON中的对应字段值
                String jsonField = JSON_FIELDS[i];
                Object value = record.get(jsonField);

                // 处理不同数据类型，空值填充为"null"
                if (value instanceof BigDecimal) {
                    cell.setCellValue(((BigDecimal) value).doubleValue());
                } else if (value instanceof Number) {
                    cell.setCellValue(((Number) value).doubleValue());
                } else if (value != null) {
                    cell.setCellValue(value.toString());
                } else {
                    cell.setCellValue("null"); // 空值填充为"null"
                }
            }
        }
    }

    /**
     * 创建表头样式
     */
    private CellStyle createHeaderCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 11);
        style.setFont(headerFont);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    /**
     * 获取输出文件路径（保存到powerjob-worker-samples/src/main/resources/data目录）
     */
    private String getOutputFilePath() throws IOException {
        // 目标路径：C:\Users\lay\Documents\GitHub\PowerJob\powerjob-worker-samples\src\main\resources\data
//        String dataDir = "C:\\Users\\lay\\Documents\\GitHub\\PowerJob\\powerjob-worker-samples\\src\\main\\resources\\data\\";
        // 获取 resources 根路径
        String resourceRoot = getClass().getClassLoader().getResource("").getPath();

        // 拼接 data 目录路径
        String dataDir = resourceRoot + "data/";
        // 确保目录存在
        File dir = new File(dataDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("无法创建目录: " + dataDir);
            }
        }

        return dataDir + "sample_output.xlsx";
    }
}
