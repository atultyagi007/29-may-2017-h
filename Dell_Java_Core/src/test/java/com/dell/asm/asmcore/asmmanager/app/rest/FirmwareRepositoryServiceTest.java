package com.dell.asm.asmcore.asmmanager.app.rest;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public class FirmwareRepositoryServiceTest {
    
    @Test
    public void testGetUniqueName() {
        List<String> emptyList = new ArrayList<>();
        List<Pair<String, List<String>>> tests = asList(
                Pair.of("Name", emptyList),
                Pair.of("Name", asList("unrelated")),
                Pair.of("Name", asList("Name - 2")),
                Pair.of("Name (2)", asList("Name")),
                Pair.of("Name (2)", asList("Name", "Name (1)")),
                Pair.of("Name (2)", asList("Name", "Name (3)")),
                Pair.of("Name (3)", asList("Name", "Name (2)")),
                Pair.of("Name (4)", asList("Name", "Name (2)", "Name (3)"))
        );

        for (Pair<String, List<String>> test : tests) {
            String expected = test.getLeft();
            List<String> similar = test.getRight();
            String actual = FirmwareRepositoryService.getUniqueName("Name", similar);
            assertEquals("Expected " + expected + " when matching names were " + similar, expected, actual);
        }
    }
}