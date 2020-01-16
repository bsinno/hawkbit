package org.eclipse.hawkbit.ui.rollout.state;

import java.io.Serializable;
import java.util.Optional;

/**
 * Stores rollout grid layout UI state according to user interactions.
 *
 */
public class RolloutLayoutUIState implements Serializable {
    private static final long serialVersionUID = 1L;

    private String searchText;

    public Optional<String> getSearchText() {
        return Optional.ofNullable(searchText);
    }

    public void setSearchText(final String searchText) {
        this.searchText = searchText;
    }

}
