package tech.powerjob.samples.BankWork;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

import java.time.LocalDate;


@Slf4j
@Component(value = "task_fix_missing_fields")
public class TaskFixMissingFields implements BasicProcessor {
    public ProcessResult process(TaskContext context) throws Exception {
        OmsLogger omsLogger = context.getOmsLogger();
        String MissingDataList = context.getWorkflowContext().fetchWorkflowContext().get("MissData");
        String UnMissingDataList = context.getWorkflowContext().fetchWorkflowContext().get("UnMissingData");
        JsonArray jsonArrayMissing = new JsonArray(MissingDataList);
        JsonArray jsonArrayUnmissing = new JsonArray(UnMissingDataList);
        JsonArray jsonArray = new JsonArray();
        jsonArray.addAll(jsonArrayMissing);
        jsonArray.addAll(jsonArrayUnmissing);
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.getJsonObject(i);
            setDefaultIfEmpty(jsonObject,"日期", LocalDate.now().toString());
            setDefaultIfEmpty(jsonObject,"币种", "CNY");
            setDefaultIfEmpty(jsonObject,"来源系统", "核心系统");
        }
        context.getWorkflowContext().appendData2WfContext("fixedData",jsonArray.toString());
        omsLogger.info("字段补全成功，结果为："+jsonArray.toString());
        return new ProcessResult(true,"字段补全成功");
    }

    public static void setDefaultIfEmpty(JsonObject obj, String field, String defaultValue) {
        if (obj.getString(field) == "" || obj.getString(field).equals(null)) {
            obj.put(field, defaultValue);
        }
    }
}
