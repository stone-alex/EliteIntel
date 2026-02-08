package elite.intel.ai.brain.handlers.commands;

import com.google.gson.JsonObject;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.SensorDataEvent;
import elite.intel.session.SystemSession;

public class ConnectionCheck implements CommandHandler {

    private final SystemSession systemSession = SystemSession.getInstance();

    @Override public void handle(String action, JsonObject params, String responseText) {

        boolean localCommandLlm = systemSession.useLocalCommandLlm();
        boolean localQueryLlm = systemSession.useLocalQueryLlm();
        String commandModel = systemSession.getLocalLlmCommandModel();
        String queryModel = systemSession.getLocalLlmQueryModel();
        boolean usingLocalLlmForCommandsAndQueries = localCommandLlm && localQueryLlm;
        boolean usingSameLllmForCommandsAndQueries = queryModel.equals(commandModel);

        StringBuilder sb = new StringBuilder();
        sb.append(" ping ");
        if (usingLocalLlmForCommandsAndQueries && usingSameLllmForCommandsAndQueries) {
            sb.append(" Model: ").append(commandModel);
        } else if(usingLocalLlmForCommandsAndQueries) {
            sb.append(" Command Model: ").append(commandModel);
            sb.append(" Query Model: ").append(queryModel);
        } else {
            sb.append(" Cloud " );
        }


        EventBusManager.publish(
                new SensorDataEvent(
                        sb.toString(),
                        usingLocalLlmForCommandsAndQueries
                                ?
                        """
                        Acknowledge connection successful.
                        Acknowledge LLM Type and models (modelName:numberOfParametersInBillions)
                        """
                                :
                        """
                        Acknowledge connection successful.
                        """
                )
        );
    }
}
