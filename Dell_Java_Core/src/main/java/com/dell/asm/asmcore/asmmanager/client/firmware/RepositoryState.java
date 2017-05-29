package com.dell.asm.asmcore.asmmanager.client.firmware;

import javax.xml.bind.annotation.XmlEnumValue;

import org.apache.log4j.Logger;

public enum RepositoryState {

    @XmlEnumValue("copying") COPYING("copying"), 
    @XmlEnumValue("errors") ERROR("errors"), 
    @XmlEnumValue("available") AVAILABLE("available");
    
    private static final Logger logger = Logger.getLogger(RepositoryState.class);
            
    private final String value;

    RepositoryState(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
    
    public static RepositoryState mapStatusToState(final RepositoryStatus status) {
        switch(status) {
            case AVAILABLE : 
                return RepositoryState.AVAILABLE;
            case COPYING :
            case PENDING :
                return RepositoryState.COPYING;
            case ERROR :
                return RepositoryState.ERROR;
            default :
                final String msg = "RepositoryStatus " + status.name() + " is not implemented for state mapping!";
                logger.warn(msg);
                throw new UnsupportedOperationException(msg);
        }
    }
}
