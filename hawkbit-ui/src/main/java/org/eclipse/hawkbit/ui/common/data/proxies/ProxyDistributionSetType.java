package org.eclipse.hawkbit.ui.common.data.proxies;

import java.util.Set;

import org.eclipse.hawkbit.repository.model.SoftwareModuleType;

public class ProxyDistributionSetType extends ProxyNamedEntity {

    private static final long serialVersionUID = 1L;
    private Long distSetTypeId;

    private String name;

    private Set<SoftwareModuleType> mandatoryModuleTypes;

    private Set<SoftwareModuleType> optionalModuleTypes;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getDistSetTypeId() {
        return distSetTypeId;
    }

    public void setDistSetTypeId(Long distSetTypeId) {
        this.distSetTypeId = distSetTypeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<SoftwareModuleType> getMandatoryModuleTypes() {
        return mandatoryModuleTypes;
    }

    public void setMandatoryModuleTypes(Set<SoftwareModuleType> mandatoryModuleTypes) {
        this.mandatoryModuleTypes = mandatoryModuleTypes;
    }

    public Set<SoftwareModuleType> getOptionalModuleTypes() {
        return optionalModuleTypes;
    }

    public void setOptionalModuleTypes(Set<SoftwareModuleType> optionalModuleTypes) {
        this.optionalModuleTypes = optionalModuleTypes;
    }
}
