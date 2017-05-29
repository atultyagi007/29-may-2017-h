/**************************************************************************
 *   Copyright (c) 2013 Dell Inc. All rights reserved.                    *
 *                                                                        *
 * DELL INC. CONFIDENTIAL AND PROPRIETARY INFORMATION. This software may  *
 * only be supplied under the terms of a license agreement or             *
 * nondisclosure agreement with Dell Inc. and may not be copied or        *
 * disclosed except in accordance with the terms of such agreement.       *
 **************************************************************************/
package com.dell.asm.asmcore.asmmanager.app.rest;

import com.dell.asm.asmcore.asmmanager.client.credential.ReferencesDTO;
import com.dell.asm.encryptionmgr.client.AbstractCredential;

public interface ICredentialRefsFetcher {
    ReferencesDTO getReferences(AbstractCredential credential);
}
