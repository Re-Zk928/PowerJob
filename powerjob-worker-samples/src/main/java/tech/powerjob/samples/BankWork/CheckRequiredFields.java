package tech.powerjob.samples.BankWork;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.powerjob.samples.tester.OmsLogPerformanceTester;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

@Slf4j
@Component(value = "check_required_fields")
public class CheckRequiredFields  implements BasicProcessor {
    public ProcessResult process(TaskContext context) throws Exception {

        OmsLogger omsLogger = context.getOmsLogger();
        String unifiedjsonList = context.getWorkflowContext().fetchWorkflowContext().get("unifiedData");
        System.out.println("接受数据");
        String[] requiredFields = { "日期", "金额", "机构", "科目", "币种", "交易码", "来源系统", "类型"};
        JsonArray jsonArray = new JsonArray(unifiedjsonList);
        JsonArray jsonArrayMissing = new JsonArray();
        JsonArray jsonArrayUnmissing = new JsonArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.getJsonObject(i);
            boolean isComplete = true;
            for(String requiredField : requiredFields) {
                if(!jsonObject.containsKey(requiredField)) {
                    jsonObject.put(requiredField, "");
                    isComplete = false;
                }
            }
            if(isComplete) {
                jsonArrayUnmissing.add(jsonObject);
            }else {
                jsonArrayMissing.add(jsonObject);
            }
        }
        String MissData = jsonArrayMissing.toString();
        String UnMissingData = jsonArrayUnmissing.toString();
        context.getWorkflowContext().appendData2WfContext("MissData", MissData);
        context.getWorkflowContext().appendData2WfContext("UnMissingData", UnMissingData);
        omsLogger.info("检查字段成功，数据缺失字段共："+jsonArrayMissing.size()+",完整字段数："+jsonArrayUnmissing.size());
        return new ProcessResult(true,"检查字段成功，数据缺失字段共："+jsonArrayMissing.size()+",完整字段数："+jsonArrayUnmissing.size());
    }
}
