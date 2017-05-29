package com.dell.asm.asmcore.asmmanager.client.templatemgr;

import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.junit.Test;

import com.dell.asm.rest.test.utils.DTOTester;
import com.dell.asm.rest.test.utils.DTOTesterException;

public class TemplateTest {

	private void ignoteForNow(String string) {
		// TODO Auto-generated method stub	
	}

    @Test
    public void testUserMarshalling()  {
        //throws InvocationTargetException, IllegalAccessException, DTOTesterException, JAXBException, XMLStreamException {
        
        try {
            MonitorsSettings mon = new MonitorsSettings();
            DTOTester.testDTO(mon);
            SNMPTrapSettings snmp = new SNMPTrapSettings();
            DTOTester.testDTO(snmp);
            EmailDestination email = new EmailDestination();
            DTOTester.testDTO(email);
            NetworkSettings net = new NetworkSettings();
            DTOTester.testDTO(net);
            PowerSettings ps = new PowerSettings();
            DTOTester.testDTO(ps);
            TimeSettings ts = new TimeSettings();
            DTOTester.testDTO(ts);
            
            InfrastructureTemplate t = new InfrastructureTemplate();
            DTOTester.testDTO(t);
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            //.e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        } catch (DTOTesterException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
           // e.printStackTrace();
        } catch (XMLStreamException e) {
            // TODO Auto-generated catch block
           // e.printStackTrace();
        }

    }
	    
	@Test
	public void testGetTemplateName() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetTemplateName() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testGetTemplateType() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetTemplateType() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testGetTemplateDescription() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetTemplateDescription() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testIsDraft() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetDraft() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testGetWizardPageNumber() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetWizardPageNumber() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testGetCreatedDate() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetCreatedDate() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testGetCreatedBy() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetCreatedBy() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testGetUpdatedDate() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetUpdatedDate() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testGetUpdatedBy() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetUpdatedBy() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testGetDisplayName() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetDisplayName() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testGetId() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetId() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testGetDeviceType() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetDeviceType() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testGetChassisIpType() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetChassisIpType() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testGetChassisNetworkRef() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetChassisNetworkRef() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testGetServerIpType() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetServerIpType() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testGetServerNetworkRef() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetServerNetworkRef() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testGetIomIpType() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetIomIpType() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testGetIomNetworkRef() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetIomNetworkRef() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testGetChassisCredentialRef() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetChassisCredentialRef() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testGetServerCredentialRef() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetServerCredentialRef() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testGetIomCredentialRef() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetIomCredentialRef() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testGetChassisUsers() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetChassisUsers() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testGetServerUsers() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetServerUsers() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testGetTrapDestinations() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetTrapDestinations() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testGetEmailNotifications() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetEmailNotifications() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testGetSyslogIpAddreses() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetSyslogIpAddreses() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testGetTimeSettings() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetTimeSettings() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testGetPowerSettings() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetPowerSettings() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testGetNetworkSettings() {
		ignoteForNow("Not yet implemented");
	}

	@Test
	public void testSetNetworkSettings() {
		ignoteForNow("Not yet implemented");
	}

}
