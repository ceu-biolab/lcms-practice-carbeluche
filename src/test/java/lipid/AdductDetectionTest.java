package lipid;

import org.drools.ruleunits.api.RuleUnitInstance;
import org.drools.ruleunits.api.RuleUnitProvider;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLOutput;
import java.util.List;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AdductDetectionTest {


    @Before
    public void setup() {
        // Optional
    }

    @Test
    public void shouldDetectAdductBasedOnMzDifference() {
        // Given two peaks with ~21.98 Da difference (e.g., [M+H]+ and [M+Na]+)
        Peak mH = new Peak(700.500, 100000.0); // [M+H]+
        Peak mNa = new Peak(722.482, 80000.0);  // [M+Na]+
        Lipid lipid = new Lipid(602, "PC 34:1", "C42H82NO8P", LipidType.PC, 34, 1);
        double annotationMZ = 700.49999d;
        double annotationIntensity = 80000.0;
        double annotationRT = 6.5d;
        Annotation annotation = new Annotation(lipid, annotationMZ, annotationIntensity, annotationRT, IoniationMode.POSITIVE, Set.of(mH, mNa));

        //annotation.detectAdductFromPeaks();

        //assertNotNull("[M+H]+ should be detected", annotation.getAdduct());
        //assertEquals( "Adduct inferred from lowest mz in group","[M+H]+", annotation.getAdduct());

        // In Drools:
        LipidScoreUnit unit = new LipidScoreUnit();
        unit.getAnnotations().add(annotation);
        RuleUnitInstance<LipidScoreUnit> instance = RuleUnitProvider.get().createRuleUnitInstance(unit);
        try{
            instance.fire();
        }finally{
            instance.close();
        }
        assertNotNull("[M+H]+ should be detected", annotation.getAdduct());
        assertEquals("[M+H]+", annotation.getAdduct());
    }


    @Test
    public void shouldDetectLossOfWaterAdduct() {
        Peak mh = new Peak(700.500, 90000.0);        // [M+H]+
        Peak mhH2O = new Peak(682.4894, 70000.0);     // [M+H–H₂O]+, ~18.0106 Da less

        Lipid lipid = new Lipid(505, "PE 36:2", "C41H78NO8P", LipidType.PE, 36, 2);
        Annotation annotation = new Annotation(lipid, mh.getMz(), mh.getIntensity(), 7.5d, IoniationMode.POSITIVE, Set.of(mh, mhH2O));

        //annotation.detectAdductFromPeaks();

        //assertNotNull("[M+H]+ should be detected", annotation.getAdduct());
        //assertEquals( "Water loss: adduct inferred from highest mz","[M+H]+", annotation.getAdduct());

        // In Drools:
        LipidScoreUnit unit = new LipidScoreUnit();
        unit.getAnnotations().add(annotation);
        RuleUnitInstance<LipidScoreUnit> instance = RuleUnitProvider.get().createRuleUnitInstance(unit);
        try{
            instance.fire(); // To fire the rules
        }finally{
            instance.close();
        }
        assertNotNull("Adduct should not be null", annotation.getAdduct());
        assertEquals("[M+H-H2O]+", annotation.getAdduct());
    }

    @Test
    public void shouldDetectDoublyChargedAdduct() {
        // Assume real M = (700.500 - 1.0073) = 699.4927
        // So [M+2H]2+ = (M + 2.0146) / 2 = 350.7536
        Peak singlyCharged = new Peak(701.50, 100000.0);  // [M+H]+
        Peak doublyCharged = new Peak(350.75, 95000.0);   // 2x = 701.500
        // (2 x 350.754) - 700.500 = 701.508 - 700.500 = 1.008
        System.out.println("Computed delta: " + Math.abs(2.0 * 350.754 - 700.500));
        // Should print: ~0.008, which is < 0.01 -> rule should fire
        Lipid lipid = new Lipid(99, "TG 54:3", "C57H104O6", LipidType.TG, 54, 3);
        // el mz base del objeto Annotation es el doubly-charged (350.754)
        Annotation annotation = new Annotation(lipid, doublyCharged.getMz(),singlyCharged.getIntensity(), 10d, IoniationMode.POSITIVE, Set.of(singlyCharged, doublyCharged));


        //assertNotNull("Adduct should not be null", annotation.getAdduct());
        //assertEquals( "Doubly charged adduct inferred correctly","[M+2H]2+", annotation.getAdduct());

        // In Drools:
        LipidScoreUnit unit = new LipidScoreUnit();
        unit.getAnnotations().add(annotation);
        RuleUnitInstance<LipidScoreUnit> instance = RuleUnitProvider.get().createRuleUnitInstance(unit);
        try{
            instance.fire();
        }finally{
            instance.close();
        }
        System.out.println("Adduct detected: " +annotation.getAdduct());
        assertNotNull("Adduct should not be null", annotation.getAdduct());
        assertEquals("[M+2H]2+", annotation.getAdduct());
    }

}
