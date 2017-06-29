package com.matrix.common;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import rapture.common.CallingContext;
import rapture.common.RaptureURI;
import rapture.common.dp.Worker;
import rapture.common.dp.WorkerStorage;
import rapture.common.exception.RaptureExceptionFactory;
import rapture.dp.InvocableUtils;
import rapture.kernel.dp.ExecutionContextUtil;

public class ConfigUtils {

    public static String getTemplateVal(Config config, String key) {
        String val = StringUtils.stripToNull(config.getString(key, null));
        if(config instanceof WorkOrderContextConfig) {
            WorkOrderContextConfig wConfig = (WorkOrderContextConfig)config;
            return ConfigUtils.renderTemplate(wConfig.getCallingContext(), wConfig.getWorkerUri(), val);
        }
        //Assume everything to be a template. $VAR should be written as ${VAR}
        return evalTemplateECF(config, val);
    }

    public static String renderTemplate(CallingContext ctx, String workerUir, String template) {
        RaptureURI workUri = new RaptureURI(workerUir);
        String workOrder = workUri.toShortString();
        Worker worker = WorkerStorage.readByFields(workOrder, workUri.getElement());
        return ExecutionContextUtil.evalTemplateECF(ctx, workOrder, template, InvocableUtils.getLocalViewOverlay(worker));
    }

    //TODO: this method is copied form ExecutionContextUtil#evalTemplateECF(...). Make ExecutionContextUtil usable for this case as well.
    public static String evalTemplateECF(Config config, String template) {
        int nut = template.indexOf("$");
        if (nut < 0) return template;
        StringBuilder sb = new StringBuilder();
        int bolt = 0;
        while (nut >= 0) {
            sb.append(template.substring(bolt, nut));
            try {
                switch (template.charAt(nut + 1)) {
                    case '$':
                        sb.append('$');
                        bolt = nut + 2;
                        break;
                    case '{':
                        int startVar = nut + 2;
                        int endVar = template.indexOf('}', nut);
                        if (endVar < 0) {
                            throw RaptureExceptionFactory.create("'${' has no matching '}' in " + template);
                        }
                        String varName = template.substring(startVar, endVar);
                        String dfault = null;
                        int idx = varName.indexOf('$');
                        if (idx >= 1) {
                            dfault = varName.substring(idx + 1);
                            varName = varName.substring(0, idx);
                        }

                        String val = config.getString(varName, "NA");
                        if (val == null) {
                            if (dfault != null) val = dfault;
                            else throw RaptureExceptionFactory.create("Variable ${" + varName + "} required but missing");
                        }
                        sb.append(val);
                        bolt = endVar + 1;
                        break;
                    default:
                        throw RaptureExceptionFactory.create("Unescaped $ with no { varName } in " + template);
                }
            } catch (IndexOutOfBoundsException ex) {
                throw RaptureExceptionFactory.create("$ with no variable in " + template);
            }
            nut = template.indexOf("$", bolt);
        }
        sb.append(template.substring(bolt));
        return sb.toString();
    }
}
