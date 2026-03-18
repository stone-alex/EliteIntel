package elite.intel.ui.controller;

import com.google.common.eventbus.Subscribe;
import elite.intel.gameapi.EventBusManager;
import elite.intel.gameapi.UserInputEvent;
import elite.intel.ui.event.AiResponseLogEvent;
import elite.intel.ui.event.AppLogDebugEvent;
import elite.intel.ui.event.AppLogEvent;
import elite.intel.ui.event.ToggleDetailedLogEvent;
import elite.intel.ui.view.AiTabPanel;

import java.util.concurrent.atomic.AtomicBoolean;

public class AiTabController {

    private final AiTabPanel view;
    private final AtomicBoolean showDetailedLog = new AtomicBoolean(false);

    public AiTabController(AiTabPanel view) {
        this.view = view;
        EventBusManager.register(this);
    }

    @Subscribe
    public void onToggleDetailedLogEvent(ToggleDetailedLogEvent event) {
        showDetailedLog.set(event.isDetailed());
    }

    @Subscribe
    public void onAppLogDebugEvent(AppLogDebugEvent event) {
        if (!showDetailedLog.get()) return;
        view.addSystemMessage(event.getData());
    }

    @Subscribe
    public void onAppLogEvent(AppLogEvent event) {
        if (event.getData() == null || event.getData().isBlank()) return;
        view.addSystemMessage(event.getData());
    }

    @Subscribe
    public void onUserInputEvent(UserInputEvent event) {
        view.addUserMessage(event.getUserInput());
    }

    @Subscribe
    public void onAiResponseLogEvent(AiResponseLogEvent event) {
        view.addAiMessage(event.getData());
    }
}
