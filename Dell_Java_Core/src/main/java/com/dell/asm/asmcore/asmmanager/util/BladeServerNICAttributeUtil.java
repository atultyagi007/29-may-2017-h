package com.dell.asm.asmcore.asmmanager.util;

public class BladeServerNICAttributeUtil {

    private BladeServerNICAttributeUtil() {
    }

    public enum vendorENUM {

        VENDOR_Broadcom, VENDOR_QLogic, VENDOR_Intel;
    }
    public enum dualQuadCheck {

       DUAL, QUAD,FC,ERROR;

        public String value() {
            return name();
        }

        public static dualQuadCheck fromValue(String v) {
            return valueOf(v);
        }

    }
    public enum vendor {

        Broadcom, Intel, QLogic;

         public String value() {
             return name();
         }

         public static vendor fromValue(String v) {
             return valueOf(v);
         }

        public static vendor fromENUMValue(vendorENUM v) {
            for (vendor ve: values()) {
                if (v.name().endsWith(ve.name()))
                    return ve;
            }
            return null;
        }

     }

    public enum NICAttribute {
        FABRIC_A1_SLOT_HALF(vendorENUM.VENDOR_Broadcom, "NIC.Integrated.1-1-1", "NIC.Integrated.1-1-2", "NIC.Integrated.1-1-3", "NIC.Integrated.1-1-4"),
        FABRIC_A2_SLOT_HALF(vendorENUM.VENDOR_Broadcom, "NIC.Integrated.1-2-1", "NIC.Integrated.1-2-2", "NIC.Integrated.1-2-3", "NIC.Integrated.1-2-4"),
        FABRIC_A3_SLOT_HALF(vendorENUM.VENDOR_Broadcom, "NIC.Integrated.1-3-1", "NIC.Integrated.1-3-2", "NIC.Integrated.1-3-3", "NIC.Integrated.1-3-4"),
        FABRIC_A4_SLOT_HALF(vendorENUM.VENDOR_Broadcom, "NIC.Integrated.1-4-1", "NIC.Integrated.1-4-2", "NIC.Integrated.1-4-3", "NIC.Integrated.1-4-4"),
        FABRIC_B1_SLOT_HALF(vendorENUM.VENDOR_Broadcom, "NIC.Mezzanine.2B-1", "NIC.Mezzanine.2B-1-2", "NIC.Mezzanine.2B-1-3", "NIC.Mezzanine.2B-1-4"),
        FABRIC_B2_SLOT_HALF(vendorENUM.VENDOR_Broadcom, "NIC.Mezzanine.2B-2", "NIC.Mezzanine.2B-2-2", "NIC.Mezzanine.2B-2-3", "NIC.Mezzanine.2B-2-4"),
        FABRIC_C1_SLOT_HALF(vendorENUM.VENDOR_Broadcom, "NIC.Mezzanine.1C-1", "NIC.Mezzanine.1C-1-2", "NIC.Mezzanine.1C-1-3", "NIC.Mezzanine.1C-1-4"),
        FABRIC_C2_SLOT_HALF(vendorENUM.VENDOR_Broadcom, "NIC.Mezzanine.1C-2", "NIC.Mezzanine.1C-2-2", "NIC.Mezzanine.1C-2-3", "NIC.Mezzanine.1C-2-4"),

        FABRIC_A1_SLOT_FULL_UPTIL_SLOT8(vendorENUM.VENDOR_Broadcom, "NIC.Integrated.1-1-1", "NIC.Integrated.1-1-2", "NIC.Integrated.1-1-3", "NIC.Integrated.1-1-4"),
        FABRIC_A1_SLOT_FULL_UPTIL_SLOT16(vendorENUM.VENDOR_Broadcom, "NIC.Integrated.2-1-1", "NIC.Integrated.2-1-2", "NIC.Integrated.2-1-3", "NIC.Integrated.2-1-4"),
        FABRIC_A2_SLOT_FULL_UPTIL_SLOT8(vendorENUM.VENDOR_Broadcom, "NIC.Integrated.1-2-1", "NIC.Integrated.1-2-2", "NIC.Integrated.1-2-3", "NIC.Integrated.1-2-4"),
        FABRIC_A2_SLOT_FULL_UPTIL_SLOT16(vendorENUM.VENDOR_Broadcom, "NIC.Integrated.2-2-1", "NIC.Integrated.2-2-2", "NIC.Integrated.2-2-3", "NIC.Integrated.2-2-4"),
        FABRIC_A3_SLOT_FULL_UPTIL_SLOT8(vendorENUM.VENDOR_Broadcom, "NIC.Integrated.1-3-1", "NIC.Integrated.1-1-2", "NIC.Integrated.1-1-3", "NIC.Integrated.1-1-4"),
        FABRIC_A3_SLOT_FULL_UPTIL_SLOT16(vendorENUM.VENDOR_Broadcom, "NIC.Integrated.2-3-1", "NIC.Integrated.2-1-2", "NIC.Integrated.2-1-3", "NIC.Integrated.2-1-4"),
        FABRIC_A4_SLOT_FULL_UPTIL_SLOT8(vendorENUM.VENDOR_Broadcom, "NIC.Integrated.1-4-1", "NIC.Integrated.1-2-2", "NIC.Integrated.1-2-3", "NIC.Integrated.1-2-4"),
        FABRIC_A4_SLOT_FULL_UPTIL_SLOT16(vendorENUM.VENDOR_Broadcom, "NIC.Integrated.2-4-1", "NIC.Integrated.2-2-2", "NIC.Integrated.2-2-3", "NIC.Integrated.2-2-4"),
        FABRIC_B1_SLOT_FULL_UPTIL_SLOT8(vendorENUM.VENDOR_Broadcom, "NIC.Mezzanine.2B-1", "NIC.Mezzanine.2B-1-2", "NIC.Mezzanine.2B-1-3", "NIC.Mezzanine.2B-1-4"),
        FABRIC_B1_SLOT_FULL_UPTIL_SLOT16(vendorENUM.VENDOR_Broadcom, "NIC.Mezzanine.4B-1", "NIC.Mezzanine.4B-1-2", "NIC.Mezzanine.4B-1-3", "NIC.Mezzanine.4B-1-4"),
        FABRIC_B2_SLOT_FULL_UPTIL_SLOT8(vendorENUM.VENDOR_Broadcom, "NIC.Mezzanine.2B-2", "NIC.Mezzanine.2B-2-2", "NIC.Mezzanine.2B-2-3", "NIC.Mezzanine.2B-2-4"), FABRIC_B2_SLOT_FULL_UPTIL_SLOT16(
                vendorENUM.VENDOR_Broadcom, "NIC.Mezzanine.4B-2", "NIC.Mezzanine.4B-2-2", "NIC.Mezzanine.4B-2-3", "NIC.Mezzanine.4B-2-4"), FABRIC_C1_SLOT_FULL_UPTIL_SLOT8(
                vendorENUM.VENDOR_Broadcom, "NIC.Mezzanine.1C-1", "NIC.Mezzanine.1C-1-2", "NIC.Mezzanine.1C-1-3", "NIC.Mezzanine.1C-1-4"), FABRIC_C1_SLOT_FULL_UPTIL_SLOT16(
                vendorENUM.VENDOR_Broadcom, "NIC.Mezzanine.3C-1", "NIC.Mezzanine.3C-1-2", "NIC.Mezzanine.3C-1-3", "NIC.Mezzanine.3C-1-4"), FABRIC_C2_SLOT_FULL_UPTIL_SLOT8(
                vendorENUM.VENDOR_Broadcom, "NIC.Mezzanine.1C-2", "NIC.Mezzanine.1C-2-2", "NIC.Mezzanine.1C-2-3", "NIC.Mezzanine.1C-2-4"), FABRIC_C2_SLOT_FULL_UPTIL_SLOT16(
                vendorENUM.VENDOR_Broadcom, "NIC.Mezzanine.3C-2", "NIC.Mezzanine.3C-2-2", "NIC.Mezzanine.3C-2-3", "NIC.Mezzanine.3C-2-4"), FABRIC_A1_SLOT_QUARTER(
                vendorENUM.VENDOR_Broadcom, "NIC.Integrated.1-1-1", "NIC.Integrated.1-1-2", "NIC.Integrated.1-1-3", "NIC.Integrated.1-1-4"), FABRIC_A2_SLOT_QUARTER(
                vendorENUM.VENDOR_Broadcom, "NIC.Integrated.1-2-1", "NIC.Integrated.1-2-2", "NIC.Integrated.1-2-3", "NIC.Integrated.1-2-4"), FABRIC_A3_SLOT_QUARTER(
                vendorENUM.VENDOR_Broadcom, "NIC.Integrated.1-3-1", "NIC.Integrated.1-3-2", "NIC.Integrated.1-3-3", "NIC.Integrated.1-3-4"), FABRIC_A4_SLOT_QUARTER(
                vendorENUM.VENDOR_Broadcom, "NIC.Integrated.1-4-1", "NIC.Integrated.1-4-2", "NIC.Integrated.1-4-3", "NIC.Integrated.1-4-4"), FABRIC_B1_SLOT_QUARTER(
                vendorENUM.VENDOR_Broadcom, "NIC.Mezzanine.1B-1", "NIC.Mezzanine.1B-1-2", "NIC.Mezzanine.1B-1-3", "NIC.Mezzanine.1B-1-4"), FABRIC_B2_SLOT_QUARTER(
                vendorENUM.VENDOR_Broadcom, "NIC.Mezzanine.1B-2", "NIC.Mezzanine.1B-2-2", "NIC.Mezzanine.1B-2-3", "NIC.Mezzanine.1B-2-4"), FABRIC_C1_SLOT_QUARTER(
                vendorENUM.VENDOR_Broadcom, "NIC.Mezzanine.1C-1", "NIC.Mezzanine.1C-1-2", "NIC.Mezzanine.1C-1-3", "NIC.Mezzanine.1C-1-4"), FABRIC_C2_SLOT_QUARTER(
                vendorENUM.VENDOR_Broadcom, "NIC.Mezzanine.1C-2", "NIC.Mezzanine.1C-2-2", "NIC.Mezzanine.1C-2-3", "NIC.Mezzanine.1C-2-4"), FABRIC_A1_FC_SLOT_HALF(
                vendorENUM.VENDOR_Broadcom, "NIC.Integrated.1-1-1", null, null, null), FABRIC_A2_FC_SLOT_HALF(vendorENUM.VENDOR_Broadcom,
                "NIC.Integrated.1-2-1", null, null, null), FABRIC_B1_FC_SLOT_HALF(vendorENUM.VENDOR_QLogic, "FC.Mezzanine.2B-1", null, null, null), FABRIC_B2_FC_SLOT_HALF(
                vendorENUM.VENDOR_QLogic, "FC.Mezzanine.2B-2", null, null, null), FABRIC_C1_FC_SLOT_HALF(vendorENUM.VENDOR_QLogic,
                "FC.Mezzanine.1C-1", null, null, null), FABRIC_C2_FC_SLOT_HALF(vendorENUM.VENDOR_QLogic, "FC.Mezzanine.1C-2", null, null, null), FABRIC_A1_FC_SLOT_FULL_UPTIL_SLOT8(
                vendorENUM.VENDOR_Broadcom, "NIC.Integrated.1-1-1", null, null, null), FABRIC_A1_FC_SLOT_FULL_UPTIL_SLOT16(
                vendorENUM.VENDOR_Broadcom, "NIC.Integrated.2-1-1", null, null, null), FABRIC_A2_FC_SLOT_FULL_UPTIL_SLOT8(
                vendorENUM.VENDOR_Broadcom, "NIC.Integrated.1-2-1", null, null, null), FABRIC_A2_FC_SLOT_FULL_UPTIL_SLOT16(
                vendorENUM.VENDOR_Broadcom, "NIC.Integrated.2-2-1", null, null, null), FABRIC_B1_FC_SLOT_FULL_UPTIL_SLOT8(vendorENUM.VENDOR_QLogic,
                "FC.Mezzanine.2B-1", null, null, null), FABRIC_B1_FC_SLOT_FULL_UPTIL_SLOT16(vendorENUM.VENDOR_QLogic, "FC.Mezzanine.4B-1", null,
                null, null), FABRIC_B2_FC_SLOT_FULL_UPTIL_SLOT8(vendorENUM.VENDOR_QLogic, "FC.Mezzanine.2B-2", null, null, null), FABRIC_B2_FC_SLOT_FULL_UPTIL_SLOT16(
                vendorENUM.VENDOR_QLogic, "FC.Mezzanine.4B-2", null, null, null), FABRIC_C1_FC_SLOT_FULL_UPTIL_SLOT8(vendorENUM.VENDOR_QLogic,
                "FC.Mezzanine.1C-1", null, null, null), FABRIC_C1_FC_SLOT_FULL_UPTIL_SLOT16(vendorENUM.VENDOR_QLogic, "FC.Mezzanine.3C-1", null,
                null, null), FABRIC_C2_FC_SLOT_FULL_UPTIL_SLOT8(vendorENUM.VENDOR_QLogic, "FC.Mezzanine.1C-2", null, null, null), FABRIC_C2_FC_SLOT_FULL_UPTIL_SLOT16(
                vendorENUM.VENDOR_QLogic, "FC.Mezzanine.3C-2", null, null, null),
                FABRIC_A1_FC_SLOT_QUARTER(vendorENUM.VENDOR_Broadcom,"NIC.Integrated.1-1-1", null, null, null),
                FABRIC_A2_FC_SLOT_QUARTER(vendorENUM.VENDOR_Broadcom, "NIC.Integrated.1-2-1", null, null, null),
                FABRIC_B1_FC_SLOT_QUARTER(vendorENUM.VENDOR_QLogic, "FC.Mezzanine.1B-1", null, null, null),
                FABRIC_B2_FC_SLOT_QUARTER(vendorENUM.VENDOR_QLogic, "FC.Mezzanine.1B-2", null, null, null),
                FABRIC_C1_FC_SLOT_QUARTER(vendorENUM.VENDOR_QLogic, "FC.Mezzanine.1C-1", null, null, null),
                FABRIC_C2_FC_SLOT_QUARTER(vendorENUM.VENDOR_QLogic, "FC.Mezzanine.1C-2", null, null, null),
                FX_B1_FC_SLOT_7(vendorENUM.VENDOR_QLogic, "FC.ChassisSlot.7-1", null, null, null),
                FX_B1_FC_SLOT_5(vendorENUM.VENDOR_QLogic, "FC.ChassisSlot.5-1", null, null, null),
                FX_B1_FC_SLOT_3(vendorENUM.VENDOR_QLogic, "FC.ChassisSlot.3-1", null, null, null),
                FX_B1_FC_SLOT_1(vendorENUM.VENDOR_QLogic, "FC.ChassisSlot.1-1", null, null, null),
                FX_B2_FC_SLOT_7(vendorENUM.VENDOR_QLogic, "FC.ChassisSlot.7-2", null, null, null),
                FX_B2_FC_SLOT_5(vendorENUM.VENDOR_QLogic, "FC.ChassisSlot.5-2", null, null, null),
                FX_B2_FC_SLOT_3(vendorENUM.VENDOR_QLogic, "FC.ChassisSlot.3-2", null, null, null),
                FX_B2_FC_SLOT_1(vendorENUM.VENDOR_QLogic, "FC.ChassisSlot.1-2", null, null, null),
                FX_C1_FC_SLOT_8(vendorENUM.VENDOR_QLogic, "FC.ChassisSlot.8-1", null, null, null),
                FX_C1_FC_SLOT_6(vendorENUM.VENDOR_QLogic, "FC.ChassisSlot.6-1", null, null, null),
                FX_C1_FC_SLOT_4(vendorENUM.VENDOR_QLogic, "FC.ChassisSlot.4-1", null, null, null),
                FX_C1_FC_SLOT_2(vendorENUM.VENDOR_QLogic, "FC.ChassisSlot.2-1", null, null, null),
                FX_C2_FC_SLOT_8(vendorENUM.VENDOR_QLogic, "FC.ChassisSlot.8-2", null, null, null),
                FX_C2_FC_SLOT_6(vendorENUM.VENDOR_QLogic, "FC.ChassisSlot.6-2", null, null, null),
                FX_C2_FC_SLOT_4(vendorENUM.VENDOR_QLogic, "FC.ChassisSlot.4-2", null, null, null),
                FX_C2_FC_SLOT_2(vendorENUM.VENDOR_QLogic, "FC.ChassisSlot.2-2", null, null, null),

                FX_A1_SLOT_HALF(vendorENUM.VENDOR_Broadcom, "NIC.Integrated.1-1-1", "NIC.Integrated.1-1-2", "NIC.Integrated.1-1-3","NIC.Integrated.1-1-4"),
                FX_A2_SLOT_HALF(vendorENUM.VENDOR_Broadcom, "NIC.Integrated.1-2-1", "NIC.Integrated.1-2-2", "NIC.Integrated.1-2-3","NIC.Integrated.1-2-4"),
                FX_A3_SLOT_HALF(vendorENUM.VENDOR_Broadcom, "NIC.Integrated.1-3-1", "NIC.Integrated.1-3-2", "NIC.Integrated.1-3-3","NIC.Integrated.1-3-4"),
                FX_A4_SLOT_HALF(vendorENUM.VENDOR_Broadcom, "NIC.Integrated.1-4-1", "NIC.Integrated.1-4-2", "NIC.Integrated.1-4-3","NIC.Integrated.1-4-4"),

                // Because fqdd is at least sometimes called Embedded
                FX_A1_SLOT_HALF_E(vendorENUM.VENDOR_Broadcom, "NIC.Embedded.1-1-1", "NIC.Embedded.1-1-2", "NIC.Embedded.1-1-3","NIC.Embedded.1-1-4"),
                FX_A2_SLOT_HALF_E(vendorENUM.VENDOR_Broadcom, "NIC.Embedded.2-1-1", "NIC.Embedded.2-1-2", "NIC.Embedded.2-1-3","NIC.Embedded.2-1-4"),
                FX_A3_SLOT_HALF_E(vendorENUM.VENDOR_Broadcom, "NIC.Embedded.3-1-1", "NIC.Embedded.1-3-2", "NIC.Embedded.1-3-3","NIC.Embedded.1-3-4"),
                FX_A4_SLOT_HALF_E(vendorENUM.VENDOR_Broadcom, "NIC.Embedded.1-4-1", "NIC.Embedded.1-4-2", "NIC.Embedded.1-4-3","NIC.Embedded.1-4-4"),

                FX_B1_SLOT_7(vendorENUM.VENDOR_Broadcom, "NIC.ChassisSlot.7-1-1", "NIC.ChassisSlot.7-1-2", "NIC.ChassisSlot.7-1-3", "NIC.ChassisSlot.7-1-4"),
                FX_B1_SLOT_5(vendorENUM.VENDOR_Broadcom, "NIC.ChassisSlot.5-1-1", "NIC.ChassisSlot.5-1-2", "NIC.ChassisSlot.5-1-3", "NIC.ChassisSlot.5-1-4"),
                FX_B1_SLOT_3(vendorENUM.VENDOR_Broadcom, "NIC.ChassisSlot.3-1-1", "NIC.ChassisSlot.3-1-2", "NIC.ChassisSlot.3-1-3", "NIC.ChassisSlot.3-1-4"),
                FX_B1_SLOT_1(vendorENUM.VENDOR_Broadcom, "NIC.ChassisSlot.1-1-1", "NIC.ChassisSlot.1-1-2", "NIC.ChassisSlot.1-1-3", "NIC.ChassisSlot.1-1-4"),
                FX_B2_SLOT_7(vendorENUM.VENDOR_Broadcom, "NIC.ChassisSlot.7-2-1", "NIC.ChassisSlot.7-2-2", "NIC.ChassisSlot.7-2-3", "NIC.ChassisSlot.7-2-4"),
                FX_B2_SLOT_5(vendorENUM.VENDOR_Broadcom, "NIC.ChassisSlot.5-2-1", "NIC.ChassisSlot.5-2-2", "NIC.ChassisSlot.5-2-3", "NIC.ChassisSlot.5-2-4"),
                FX_B2_SLOT_3(vendorENUM.VENDOR_Broadcom, "NIC.ChassisSlot.3-2-1", "NIC.ChassisSlot.3-2-2", "NIC.ChassisSlot.3-2-3", "NIC.ChassisSlot.3-2-4"),
                FX_B2_SLOT_1(vendorENUM.VENDOR_Broadcom, "NIC.ChassisSlot.1-2-1", "NIC.ChassisSlot.1-2-2", "NIC.ChassisSlot.1-2-3", "NIC.ChassisSlot.1-2-4"),
                FX_C1_SLOT_8(vendorENUM.VENDOR_Broadcom, "NIC.ChassisSlot.8-1-1", "NIC.ChassisSlot.8-1-2", "NIC.ChassisSlot.8-1-3", "NIC.ChassisSlot.8-1-4"),
                FX_C1_SLOT_6(vendorENUM.VENDOR_Broadcom, "NIC.ChassisSlot.6-1-1", "NIC.ChassisSlot.6-1-2", "NIC.ChassisSlot.6-1-3", "NIC.ChassisSlot.6-1-4"),
                FX_C1_SLOT_4(vendorENUM.VENDOR_Broadcom, "NIC.ChassisSlot.4-1-1", "NIC.ChassisSlot.4-1-2", "NIC.ChassisSlot.4-1-3", "NIC.ChassisSlot.4-1-4"),
                FX_C1_SLOT_2(vendorENUM.VENDOR_Broadcom, "NIC.ChassisSlot.2-1-1", "NIC.ChassisSlot.2-1-2", "NIC.ChassisSlot.2-1-3", "NIC.ChassisSlot.2-1-4"),
                FX_C2_SLOT_8(vendorENUM.VENDOR_Broadcom, "NIC.ChassisSlot.8-2-1", "NIC.ChassisSlot.8-2-2", "NIC.ChassisSlot.8-2-3", "NIC.ChassisSlot.8-2-4"),
                FX_C2_SLOT_6(vendorENUM.VENDOR_Broadcom, "NIC.ChassisSlot.6-2-1", "NIC.ChassisSlot.6-2-2", "NIC.ChassisSlot.6-2-3", "NIC.ChassisSlot.6-2-4"),
                FX_C2_SLOT_4(vendorENUM.VENDOR_Broadcom, "NIC.ChassisSlot.4-2-1", "NIC.ChassisSlot.4-2-2", "NIC.ChassisSlot.4-2-3", "NIC.ChassisSlot.4-2-4"),
                FX_C2_SLOT_2(vendorENUM.VENDOR_Broadcom, "NIC.ChassisSlot.2-2-1", "NIC.ChassisSlot.2-2-2", "NIC.ChassisSlot.2-2-3", "NIC.ChassisSlot.2-2-4")
        ;

        private final vendorENUM vendor;
        private final String partitionOne;
        private final String partitionTwo;
        private final String partitionThree;
        private final String partitonFour;

        private NICAttribute(vendorENUM vendor, String partitionOne, String partitionTwo, String partitionThree, String partitonFour) {

            this.vendor = vendor;
            this.partitionOne = partitionOne;
            this.partitionTwo = partitionTwo;
            this.partitionThree = partitionThree;
            this.partitonFour = partitonFour;

        }

        public vendorENUM getVendor() {
            return vendor;
        }

        public String getPartitionOne() {
            return partitionOne;
        }

        public String getPartitionTwo() {
            return partitionTwo;
        }

        public String getPartitionThree() {
            return partitionThree;
        }

        public String getPartitonFour() {
            return partitonFour;
        }

        public static boolean matchesPartition(String fabricPrefix, String svendor, String fqdd ) {
            for (NICAttribute na: values()) {
                if (na.name().startsWith(fabricPrefix)) {
                             if (fqdd.equals(na.getPartitionOne()) ||
                                fqdd.equals(na.getPartitionTwo()) ||
                                fqdd.equals(na.getPartitionThree()) ||
                                fqdd.equals(na.getPartitonFour()))
                            return true;
                    
                }
            }
            return false;
        }

    }
}