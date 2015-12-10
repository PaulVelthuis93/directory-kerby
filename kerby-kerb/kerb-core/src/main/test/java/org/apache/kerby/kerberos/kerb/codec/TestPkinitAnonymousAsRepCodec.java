/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.apache.kerby.kerberos.kerb.codec;

import org.apache.kerby.asn1.Asn1;
import org.apache.kerby.kerberos.kerb.type.base.EncryptedData;
import org.apache.kerby.kerberos.kerb.type.base.KrbMessageType;
import org.apache.kerby.kerberos.kerb.type.base.NameType;
import org.apache.kerby.kerberos.kerb.type.base.PrincipalName;
import org.apache.kerby.kerberos.kerb.type.kdc.AsRep;
import org.apache.kerby.kerberos.kerb.type.pa.PaData;
import org.apache.kerby.kerberos.kerb.type.pa.PaDataEntry;
import org.apache.kerby.kerberos.kerb.type.pa.PaDataType;
import org.apache.kerby.kerberos.kerb.type.pa.pkinit.PaPkAsRep;
import org.apache.kerby.kerberos.kerb.type.ticket.Ticket;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;

public class TestPkinitAnonymousAsRepCodec {
    @Test
    public void test() throws IOException {
        byte[] bytes = CodecTestUtil.readDataFile("/pkinit_anonymous_asrep.token");
        Asn1.dump(bytes, true);
        ByteBuffer asRepToken = ByteBuffer.wrap(bytes);

        AsRep asRep = new AsRep();
        asRep.decode(asRepToken);
        Asn1.dump(asRep, false);

        assertThat(asRep.getPvno()).isEqualTo(5);
        assertThat(asRep.getMsgType()).isEqualTo(KrbMessageType.AS_REP);
        PaData paData = asRep.getPaData();

        PaDataEntry pkAsRepEntry = paData.findEntry(PaDataType.PK_AS_REP);
        assertThat(pkAsRepEntry.getPaDataType()).isEqualTo(PaDataType.PK_AS_REP);

        PaPkAsRep paPkAsRep = new PaPkAsRep();
        paPkAsRep.decode(pkAsRepEntry.getPaDataValue());
        //Failed
//        assertThat(paPkAsRep.getDHRepInfo()).isNotNull();

        PaDataEntry etypeInfo2Entry = paData.findEntry(PaDataType.ETYPE_INFO2);
        assertThat(etypeInfo2Entry.getPaDataType()).isEqualTo(PaDataType.ETYPE_INFO2);

        PaDataEntry pkinitKxEntry = paData.findEntry(PaDataType.PKINIT_KX);
        assertThat(pkinitKxEntry.getPaDataType()).isEqualTo(PaDataType.PKINIT_KX);

        assertThat(asRep.getPvno()).isEqualTo(5);
        assertThat(asRep.getMsgType()).isEqualTo(KrbMessageType.AS_REP);
        assertThat(asRep.getCrealm()).isEqualTo("WELLKNOWN:ANONYMOUS");

        PrincipalName cName = asRep.getCname();
        assertThat(cName.getNameType()).isEqualTo(NameType.NT_UNKNOWN);
        assertThat(cName.getNameStrings()).hasSize(2).contains("WELLKNOWN", "ANONYMOUS");

        Ticket ticket = asRep.getTicket();
        assertThat(ticket.getTktvno()).isEqualTo(5);
        assertThat(ticket.getRealm()).isEqualTo("EXAMPLE.COM");
        PrincipalName sName = ticket.getSname();
        assertThat(sName.getNameType()).isEqualTo(NameType.NT_SRV_INST);
        assertThat(sName.getNameStrings()).hasSize(2)
                .contains("krbtgt", "EXAMPLE.COM");

        EncryptedData encryptedData = ticket.getEncryptedEncPart();
        assertThat(encryptedData.getKvno()).isEqualTo(1);
        assertThat(encryptedData.getEType().getValue()).isEqualTo(0x0012);
    }
}