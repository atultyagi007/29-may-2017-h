/**
 *
 */
package com.dell.asm.asmcore.asmmanager.client.credential;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.builder.ToStringBuilder;


@XmlRootElement(name = "credentialList")
public class AsmCredentialListDTO {
    private List<AsmCredentialDTO> credentialList = new ArrayList<>();
    private int totalRecords = 0;

    @XmlElement(name = "credential")
    public List<AsmCredentialDTO> getCredentialList() {
        return credentialList;
    }

    public void setCredentialList(List<AsmCredentialDTO> viewObjectList) {
        this.credentialList = viewObjectList;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("credentialList", credentialList)
                .toString();
    }
}
