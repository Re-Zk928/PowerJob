package tech.powerjob.samples.BankWork;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;
import tech.powerjob.worker.log.OmsLogger;

@Slf4j
@Component(value = "check_required_fields")
public class CheckRequiredFields implements BasicProcessor{
    public ProcessResult process(TaskContext context) throws Exception {
        return null;
    }
}
