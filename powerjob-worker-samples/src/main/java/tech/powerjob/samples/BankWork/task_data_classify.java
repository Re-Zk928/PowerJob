package tech.powerjob.samples.BankWork;
import com.fasterxml.jackson.core.type.TypeReference;
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
        Map fieldMapping = loadFieldMapping();
        String jsonStr = context.getWorkflowContext().fetchWorkflowContext().get("bank_data");
        List<Map<String, Object>> rawList = JsonUtils.parseObject(jsonStr, new TypeReference<List<Map<String, Object>>>() {
        });

        List<Map<String, Object>> standardizedList = new ArrayList<>();
        // PowerJob 在线日志功能，使用该 Logger 打印的日志可以直接在 PowerJob 控制台查看
        OmsLogger omsLogger = context.getOmsLogger();
        context.getOmsLogger().info("FirstTask 执行");
        for (Map<String, Object> original : rawList) {
            Map<String, Object> standardized = new HashMap<>();
            for (Map.Entry<String, Object> entry : original.entrySet()) {
                String rawKey = entry.getKey();
                Object value = entry.getValue();
                String stdKey = (String) fieldMapping.getOrDefault(rawKey, rawKey); // 没匹配则保留原名
                standardized.put(stdKey, value);
            }
            standardizedList.add(standardized);
        }

        return new ProcessResult(true, "task_data_classify 任务执行完成");
    }

}
