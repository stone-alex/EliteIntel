package elite.intel.ui.view;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Keeps a single selected binding row across multiple grouped tables.
 * <p>
 * The Bindings tab renders many independent JTable instances, so selection
 * cannot rely on one shared table model.
 */
class BindingsSelectionController {
    private final List<JTable> tables = new ArrayList<>();
    private boolean clearingSelection;
    private SelectedBinding selectedBinding;

    void register(JTable table) {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tables.add(table);
        table.getSelectionModel().addListSelectionListener(event -> {
            if (event.getValueIsAdjusting() || clearingSelection)
                return;

            int selectedRow = table.getSelectedRow();
            if (selectedRow < 0) {
                if (selectedBinding != null && selectedBinding.table() == table) {
                    selectedBinding = null;
                }
                return;
            }

            selectRow(table, selectedRow);
        });
    }

    String selectRow(JTable table, int row) {
        clearingSelection = true;
        for (JTable other : tables) {
            if (other != table) {
                other.clearSelection();
            }
        }
        table.getSelectionModel().setSelectionInterval(row, row);
        clearingSelection = false;

        Object value = table.getValueAt(row, 0);
        String bindingId = value == null ? "" : value.toString();
        selectedBinding = new SelectedBinding(table, bindingId);
        return bindingId;
    }

    void clearSelection() {
        clearingSelection = true;
        for (JTable table : tables) {
            table.clearSelection();
        }
        clearingSelection = false;
        selectedBinding = null;
    }

    void resetTables() {
        clearSelection();
        tables.clear();
    }

    Optional<String> selectedBindingId() {
        return Optional.ofNullable(selectedBinding).map(SelectedBinding::bindingId);
    }

    private record SelectedBinding(JTable table, String bindingId) {
    }
}
