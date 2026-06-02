package elite.intel.ui.view;

import elite.intel.ai.hands.BindingSaveResult;

import javax.swing.*;
import java.awt.*;

import static elite.intel.ui.i18n.MultiLingualTextProvider.getText;
import static elite.intel.ui.view.AppTheme.DISABLED_FG;
import static elite.intel.ui.view.AppTheme.FG_MUTED;

class BindingSaveResultPresenter {
    private final Component parent;
    private final JLabel statusLabel;

    BindingSaveResultPresenter(Component parent, JLabel statusLabel) {
        this.parent = parent;
        this.statusLabel = statusLabel;
    }

    void show(BindingSaveResult result) {
        show(messageFor(result), isErrorResult(result));
    }

    void showWriteFailed() {
        show(getText("bindings.assign.writeFailed"), true);
    }

    private void show(String message, boolean error) {
        statusLabel.setForeground(error ? DISABLED_FG : FG_MUTED);
        statusLabel.setText(message);
        JOptionPane.showMessageDialog(
                parent,
                message,
                getText("bindings.assign.dialogTitle"),
                error ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE
        );
    }

    private String messageFor(BindingSaveResult result) {
        return switch (result) {
            case SAVED -> getText("bindings.assign.success");
            case NO_CHANGE -> getText("bindings.assign.noChange");
            case STALE_FILE -> getText("bindings.assign.staleFile");
            case KEY_OCCUPIED -> getText("bindings.assign.keyOccupied");
            case UNKNOWN_KEY -> getText("bindings.assign.unknownKey");
            case BINDING_NOT_FOUND -> getText("bindings.assign.bindingNotFound");
            case UNSUPPORTED_XML -> getText("bindings.assign.unsupportedXml");
            case BACKUP_FAILED -> getText("bindings.assign.backupFailed");
            case WRITE_FAILED -> getText("bindings.assign.writeFailed");
        };
    }

    private boolean isErrorResult(BindingSaveResult result) {
        return result != BindingSaveResult.SAVED && result != BindingSaveResult.NO_CHANGE;
    }
}
