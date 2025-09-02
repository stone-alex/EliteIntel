package elite.companion.ui;

import java.beans.PropertyChangeListener;

public interface DataModel {
    String getText();
    void setText(String newText);
    void addPropertyChangeListener(PropertyChangeListener listener);
    void removePropertyChangeListener(PropertyChangeListener listener);
}