package tech.powerjob.samples.BankWork;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;

import javax.annotation.processing.Processor;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@Slf4j
@Component(value = "task_data_receive")


public class task_data_receive implements BasicProcessor {

    public ProcessResult process(TaskContext context) throws Exception {

        // 1. 读取Excel文件
        List<Map<String, String>> excelData = readExcel("data/sample_data.xlsx");

        // 2. 读取JSON文件
        List<Map<String, Object>> jsonData = readJson("data/sample_data.json");

        // 3. 读取TXT文件
        List<String> txtData = readTxt("data/sample_data.txt");

        // 4. 把数据放入工作流上下文
        context.getWorkflowContext().appendData2WfContext("excelData", excelData);
        context.getWorkflowContext().appendData2WfContext("jsonData", jsonData);
        context.getWorkflowContext().appendData2WfContext("txtData", txtData);

        context.getOmsLogger().info("Excel数据行数：" + excelData.size());
        context.getOmsLogger().info("JSON数据条数：" + jsonData.size());
        context.getOmsLogger().info("TXT数据行数：" + txtData.size());

        return new ProcessResult(true, "文件读取并存入上下文完成");
    }

    private List<Map<String, String>> readExcel(String fileName) throws Exception {
        List<Map<String, String>> data = new ArrayList<>();

        try (InputStream is = task_data_receive.class.getClassLoader().getResourceAsStream(fileName);
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0); // 取第一个sheet
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return data; // 空表
            }

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                cell.setCellType(CellType.STRING);
                headers.add(cell.getStringCellValue());
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, String> rowData = new HashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        cell.setCellType(CellType.STRING);
                        rowData.put(headers.get(j), cell.getStringCellValue());
                    } else {
                        rowData.put(headers.get(j), "");
                    }
                }
                data.add(rowData);
            }
        }

        return data;
    }

    private List<Map<String, Object>> readJson(String fileName) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        try (InputStream is = task_data_receive.class.getClassLoader().getResourceAsStream(fileName)) {
            return mapper.readValue(is, new TypeReference<List<Map<String, Object>>>(){});
        }
    }

    private List<String> readTxt(String fileName) throws Exception {
        List<String> lines = new ArrayList<>();
        try (InputStream is = task_data_receive.class.getClassLoader().getResourceAsStream(fileName);
             BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {

            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }
}
