package org.eclipse.hawkbit.ui.distributions.smtype.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.hawkbit.repository.SoftwareModuleTypeManagement;
import org.eclipse.hawkbit.repository.model.SoftwareModuleType;
import org.eclipse.hawkbit.repository.model.Type;
import org.eclipse.hawkbit.ui.utils.SPUIDefinitions;
import org.eclipse.hawkbit.ui.utils.UIComponentIdProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.vaadin.server.Page;

public class SmTypeCssStylesHandler {
    private final SoftwareModuleTypeManagement softwareModuleTypeManagement;

    public SmTypeCssStylesHandler(final SoftwareModuleTypeManagement softwareModuleTypeManagement) {
        this.softwareModuleTypeManagement = softwareModuleTypeManagement;
    }

    public void updateSmTypeStyles() {
        final String recreateStylesheetScript = String.format("const stylesheet = recreateStylesheet('%s').sheet;",
                UIComponentIdProvider.SM_TYPE_COLOR_STYLE);
        final String addStyleRulesScript = buildStyleRulesScript(getSmTypeIdWithColor(getAllSmTypes()));

        Page.getCurrent().getJavaScript().execute(recreateStylesheetScript + addStyleRulesScript);
    }

    private List<SoftwareModuleType> getAllSmTypes() {
        Pageable query = PageRequest.of(0, SPUIDefinitions.PAGE_SIZE);
        Slice<SoftwareModuleType> smTypeSlice;
        final List<SoftwareModuleType> smTypes = new ArrayList<>();

        do {
            smTypeSlice = softwareModuleTypeManagement.findAll(query);
            smTypes.addAll(smTypeSlice.getContent());
        } while ((query = smTypeSlice.nextPageable()) != Pageable.unpaged());

        return smTypes;
    }

    private Map<Long, String> getSmTypeIdWithColor(final List<SoftwareModuleType> smTypes) {
        return smTypes.stream().collect(Collectors.toMap(Type::getId,
                type -> Optional.ofNullable(type.getColour()).orElse(SPUIDefinitions.DEFAULT_COLOR)));
    }

    private String buildStyleRulesScript(final Map<Long, String> typeIdWithColor) {
        return typeIdWithColor.entrySet().stream().map(entry -> {
            final String typeClass = String.join("-", UIComponentIdProvider.SM_TYPE_COLOR_CLASS,
                    String.valueOf(entry.getKey()));
            final String typeColor = entry.getValue();

            // "!important" is needed because we are overriding valo theme here
            // (alternatively we could provide more specific selector)
            return String.format(
                    "addStyleRule(stylesheet, '.%1$s, .%1$s > td, .%1$s .v-grid-cell', "
                            + "'background-color:%2$s !important; background-image: none !important;')",
                    typeClass, typeColor);
        }).collect(Collectors.joining(";"));
    }
}