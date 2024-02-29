package client.utils;

import javafx.beans.binding.StringBinding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ObservableResourceFactoryTest {
    ObservableResourceFactory sut;
    Map<String, String> testMapA;
    Map<String, String> testMapB;
    @BeforeEach
    void setUp() {
        sut = new ObservableResourceFactory();

        testMapA = new HashMap<>();
        testMapA.put("keyA", "valueA");
        testMapB = new HashMap<>();
        testMapB.put("keyA", "valueB");
    }

    /***
     * Is the Map initialized, and is that value returned by resourcesProperty()?
     */
    @Test
    void resourcesPropertyInitialized() {
        assertNotNull(sut.resourcesProperty());
    }

    /***
     * Is a Map inserted successfully once?
     */
    @Test
    void replacementAndGetterTest() {
        sut.setResources(testMapA);
        var sutEntrySet = sut.resourcesProperty().entrySet();
        assertEquals(testMapA.entrySet(), sutEntrySet);
    }

    /****
     * Does the old Map get replaced entirely by the new Map?
     */
    @Test
    void liveReplacementSet() {
        sut.setResources(testMapA);
        sut.setResources(testMapB);
        var sutEntrySetB = sut.resourcesProperty().entrySet();
        assertEquals(testMapB.entrySet(), sutEntrySetB);
    }

    /***
     * Is the correct StringBinding for a key returned?
     */
    @Test
    void getStringBinding() {
        sut.setResources(testMapA);
        var valueReturned = sut.getStringBinding("keyA");
        assertEquals("valueA", valueReturned.getValue());
    }

    /***
     * Does the binding change correctly when the Map is replaced live?
     */
    @Test
    void liveReplacementBinding() {
        sut.setResources(testMapA);
        StringBinding binding = sut.getStringBinding("keyA");
        String valueBefore = binding.getValue();
        sut.setResources(testMapB);
        String valueAfter = binding.getValue();
        assertEquals(testMapA.get("keyA"), valueBefore);
        assertEquals(testMapB.get("keyA"), valueAfter);
    }
}