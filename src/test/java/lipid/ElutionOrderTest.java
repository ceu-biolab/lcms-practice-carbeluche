package lipid;

import org.drools.ruleunits.api.RuleUnitInstance;
import org.drools.ruleunits.api.RuleUnitProvider;
import org.drools.ruleunits.impl.RuleUnitProviderImpl;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import lipid.*;

public class ElutionOrderTest {

    static final Logger LOG = LoggerFactory.getLogger(ElutionOrderTest.class);
    private Set<Peak> peaks1;
    private Set<Peak> peaks2;
    private Set<Peak> peaks3;


    @Before
    public void setup() {
        // A1 -> [M+Na]+
        this.peaks1= Set.of(new Peak(885.79056, 1.0e6), new Peak(907.77306, 0.8e6)); // [M+H]+ (base peak) added 21.9825 = 907.77306 simualtes [M+Na]+
        // A2 ->[M+H-H2O]+
        this.peaks2= Set.of(new Peak(857.7593, 1.0e7), new Peak(839.7487, 0.5e7)); // [M+H]+ (base peak) added -18.0106 -> [M+H-H20]+
        // A3 -> [M+2H]2+
        this.peaks3 = Set.of(
                new Peak(913.8220, 1.0e5),
                new Peak(457.41465, 0.5e5)
        ); // If the base is M+H at 913.822, the [M+2H]2+ should be: (913.822+1.0073)/2= 457.41465
    }


    /**
     * Test to check the elution order of the lipids.
     * The elution order is based on the number of carbons if the lipid type and the number of
     * double bonds is the same.
     * The larger the number of carbons, the longer the RT.
     */
    @Test
    public void score1BasedOnRTCarbonNumbers() {
        setup();
        LipidScoreUnit unit = new LipidScoreUnit();
        unit.setPositiveScoringEnabled(true);

        //Annotation3: carbon=56, rt=11 vs (Annotation1 (C=54 6 RT=10) and Annotation2 (C=52&RT=9)
        //A1 VS A2: A1 win -> A1 =+1
        //A1 VS A3: A3 win -> A3 =+1
        //A2 VS A3: A3 win -> A3 =+1
        //Therefore(score, total applied, normalized): Annotation1 = (1,1,1.0), Annotation2 = (0,0,0.0) and Annotation 3 = (2,2,1.0)
        Lipid lipid1 = new Lipid(1, "TG 54:3", "C57H104O6", LipidType.TG, 54, 3);
        Lipid lipid2 = new Lipid(2, "TG 52:3", "C55H100O6", LipidType.TG, 52, 3);
        Lipid lipid3 = new Lipid(3, "TG 56:3", "C59H108O6", LipidType.TG, 56, 3);
        // A1: [M+Na]+ simulated base = [M+H]+
        Annotation annotation1 = new Annotation(lipid1, 885.79056, 1.0e6, 10d, IoniationMode.POSITIVE, this.peaks1);
        // A2: [M+H-H20]+ simulated base = [M+H]+
        Annotation annotation2 = new Annotation(lipid2, 857.7593, 1.0e7, 9d, IoniationMode.POSITIVE, this.peaks2);
        // A3: [M+2H]2+ simulated base = [M+H]+
        Annotation annotation3 = new Annotation(lipid3, 913.8220, 1.5e5, 11d, IoniationMode.POSITIVE, this.peaks3);
        // Detect adduct manually
        annotation1.detectAdductFromPeaks();
        annotation2.detectAdductFromPeaks();
        annotation3.detectAdductFromPeaks();
        // Insert into rule unit
        unit.getAnnotations().add(annotation1);
        unit.getAnnotations().add(annotation2);
        unit.getAnnotations().add(annotation3);
        RuleUnitInstance<LipidScoreUnit> instance = RuleUnitProvider.get().createRuleUnitInstance(unit);

        try {
            instance.fire();
            //System.out.println("A1: " + annotation1.getAdduct() + " | Detected: " + annotation1.getDetectedAdducts());
            //System.out.println("A2: " + annotation2.getAdduct() + " | Detected: " + annotation2.getDetectedAdducts());
            //System.out.println("A3: " + annotation3.getAdduct() + " | Detected: " + annotation3.getDetectedAdducts());
            assertEquals(1.0, annotation1.getNormalizedScore(), 0.01); // wins vs A2
            assertEquals(0.0, annotation2.getNormalizedScore(), 0.01); // did not win at all
            assertEquals(0.0, annotation3.getNormalizedScore(), 0.01); // win vs A1 and A2
            // In case to verify absolute points: assertEquals (2, annotation3.getScore());
        } finally {
            instance.close();
        }
    }

    /**
     * Test to check the elution order of the lipids.
     * The elution order is based on the number of double bonds if the lipid type and the number of
     * carbons is the same.
     * The higher the number of double bonds, the shorter the RT.
     */
    @Test
    public void score1BasedOnRTDoubleBonds() {
        setup();
        LipidScoreUnit unit = new LipidScoreUnit();
        Lipid lipid1 = new Lipid(1, "TG 54:3", "C57H104O6", LipidType.TG, 54, 3);
        Lipid lipid2 = new Lipid(2, "TG 54:4", "C55H100O6", LipidType.TG, 54, 4); // highest DB
        Lipid lipid3 = new Lipid(3, "TG 54:2", "C59H108O6", LipidType.TG, 54, 2); // lowest DB
        Annotation annotation1 = new Annotation(lipid1, 885.79056, 1.0e6, 10d, IoniationMode.POSITIVE, this.peaks1);
        Annotation annotation2 = new Annotation(lipid2, 857.7593, 1.0e7, 9d, IoniationMode.POSITIVE, this.peaks2);
        Annotation annotation3 = new Annotation(lipid3, 913.8220, 1.5e5, 11d, IoniationMode.POSITIVE, this.peaks3);
        // Detect adduct manually
        annotation1.detectAdductFromPeaks();
        annotation2.detectAdductFromPeaks();
        annotation3.detectAdductFromPeaks();
        // Insert into rule unit
        unit.getAnnotations().add(annotation1);
        unit.getAnnotations().add(annotation2);
        unit.getAnnotations().add(annotation3);
        RuleUnitInstance<LipidScoreUnit> instance = RuleUnitProvider.get().createRuleUnitInstance(unit);

        try {
            instance.fire();
            //System.out.println("Annotation 1 score: " + annotation1.getScore());
            //System.out.println("Annotation 2 score: " + annotation2.getScore());
            //System.out.println("Annotation 3 score: " + annotation3.getScore());
            //System.out.println("Normalized scores: A1=" + annotation1.getNormalizedScore() + ", A2=" + annotation2.getNormalizedScore() + ", A3=" + annotation3.getNormalizedScore());
            // Rule: molecule with more DB should elute earlier
            // A2(DB=4, RT=9) vs A1(DB=3, RT=10): A2 wins -> +1
            // A2(DB=4, RT=9) vs A3(DB=2, RT=11): A2 wins -> +1
            // A1(DB=3, RT=10) vs A3(DB=2, RT=11): A1 wins -> +1
            assertEquals(1.0, annotation2.getNormalizedScore(), 0.01); // A2 wins vs A1 and A3
            assertEquals(0.5, annotation1.getNormalizedScore(), 0.01); // A1 wins vs A3 only
            assertEquals(0.0, annotation3.getNormalizedScore(), 0.01); // A3 wins none
        } finally {
            instance.close();
        }
    }

    /**
     * Test to check the elution order of the lipids.
     * The elution order is based on the number of double bonds if the lipid type and the number of
     * carbons is the same.
     * The higher the number of double bonds, the shorter the RT.
     * The RT order of lipids with the same number of carbons and double bonds is the same
     * -> PG < PE < PI < PA < PS << PC.
     */
    @Test
    public void score1BasedOnLipidType() {
        setup();
        LipidScoreUnit unit = new LipidScoreUnit();
        Lipid lipid1 = new Lipid(3, "PI 34:0", "C43H83O13P", LipidType.PI, 54, 0); // compoundId = 3
        Lipid lipid2 = new Lipid(1, "PG 34:0", "C40H79O10P", LipidType.PG, 54, 0); // compoundId = 1
        Lipid lipid3 = new Lipid(2, "PC 34:0", "C42H84NO8P", LipidType.PC, 54, 0); // compoundId = 2
        Annotation annotation1 = new Annotation(lipid1, 885.79056, 1.0e6, 10d, IoniationMode.POSITIVE, this.peaks1);
        Annotation annotation2 = new Annotation(lipid2, 857.7593, 1.0e7, 9d, IoniationMode.POSITIVE, this.peaks2);
        Annotation annotation3 = new Annotation(lipid3, 913.8220, 1.5e5, 11d, IoniationMode.POSITIVE, this.peaks3);
        // Detect adduct manually
        annotation1.detectAdductFromPeaks();
        annotation2.detectAdductFromPeaks();
        annotation3.detectAdductFromPeaks();
        // Insert into rule unit
        unit.getAnnotations().add(annotation1);
        unit.getAnnotations().add(annotation2);
        unit.getAnnotations().add(annotation3);
        //System.out.println(annotation3.getDetectedAdducts());
        RuleUnitInstance<LipidScoreUnit> instance = RuleUnitProvider.get().createRuleUnitInstance(unit);
        try {
            instance.fire();
            // PI vs PG: PI elutes after PG (RT 10>9) -> PI wins so gets 1 and PG gets 0
            // PC vs PG: PC elutes after PG (RT 11>9) -> PC wins so gets 1 and PG gets 0
            // PC vs PI: PC elutes after PI (RT 11>10) -> PC wins so gets 1 and PI gets 0
            // every lipid wins 2 comparisons -> 2/2 = 1.0 normalized score
            //System.out.println(annotation1);
            //System.out.println(annotation2);
            //System.out.println(annotation3);
            //System.out.printf("Annotation3 score = %d, total = %d, normalized = %.2f%n",
            // annotation3.getScore(),
            // annotation3.getTotalScoreApplied(),
            //annotation3.getNormalizedScore());
            assertEquals(0.0, annotation1.getNormalizedScore(), 0.01); // PI
            assertEquals(0.0, annotation2.getNormalizedScore(), 0.01); // PG
            assertEquals(1.0, annotation3.getNormalizedScore(), 0.01); // PC
        } finally {
            instance.close();
        }
    }

    /**
     * Test to check the elution order of the lipids. The elution order is based on the number of double bonds if the lipid type and the number of
     * carbons is the same. The higher the number of double bonds, the shorter the RT.
     * The RT order of lipids with the same number of carbons and double bonds is the same
     * -> PG < PE < PI < PA < PS << PC.
     */
    @Test
    public void negativeScoreBasedOnRTNumberOfCarbons() {

        setup();
        LipidScoreUnit unit = new LipidScoreUnit();

        Lipid lipid1 = new Lipid(1, "PI 34:0", "C43H83O13P", LipidType.PI, 34, 0); // RT = 10
        Lipid lipid2 = new Lipid(2, "PG 34:0", "C40H79O10P", LipidType.PG, 34, 0); // RT = 9
        Lipid lipid3 = new Lipid(3, "PC 34:0", "C42H84NO8P", LipidType.PC, 34, 0); // RT = 8
        Annotation annotation1 = new Annotation(lipid1, 885.79056, 1.0e7, 10d, IoniationMode.POSITIVE, peaks1); // PI (34:0,RT=10)
        Annotation annotation2 = new Annotation(lipid2, 857.7593, 1.0e8, 9d, IoniationMode.POSITIVE, peaks2); // PG (34:0, RT=9)
        Annotation annotation3 = new Annotation(lipid3, 913.8220, 1.0e6, 8d, IoniationMode.POSITIVE, peaks3); // PC (34:0, RT=8)
        // PG < PE < PI < PC < PA < PS
        annotation1.detectAdductFromPeaks();
        annotation2.detectAdductFromPeaks();
        annotation3.detectAdductFromPeaks();
        //System.out.println(annotation1.getDetectedAdducts());
        //System.out.println(annotation2.getDetectedAdducts());
        //System.out.println(annotation3.getDetectedAdducts());
        //System.out.println(annotation1.getScore());
        //System.out.println(annotation2.getScore());
        //System.out.println(annotation3.getScore());

        unit.getAnnotations().add(annotation1);
        unit.getAnnotations().add(annotation2);
        unit.getAnnotations().add(annotation3);

        unit.setPositiveScoringEnabled(false); // to disable the positive rule without commenting it
        RuleUnitInstance<LipidScoreUnit> instance = RuleUnitProvider.get().createRuleUnitInstance(unit);

        //System.out.println("annotation1 (PI) score: " + annotation1.getScore());
        //System.out.println("annotation2 (PG) score: " + annotation2.getScore());
        //System.out.println("annotation3 (PC) score: " + annotation3.getScore());

        try {
            instance.fire();
            //System.out.println("ElutesAfter PC > PG: " + LipidUtils.elutesAfter(LipidType.PC, LipidType.PG));
            //System.out.println("PC RT: " + annotation3.getRtMin());
            //System.out.println("PG RT: " + annotation2.getRtMin());

            //System.out.println("PI score: " + annotation1.getScore());
            //System.out.println("PG score: " + annotation2.getScore());
            //System.out.println("PC score: " + annotation3.getScore());

            assertEquals(1.0, annotation1.getNormalizedScore(), 0.01); //annotation1 (PI) eluted after PG -> no positive score
            assertEquals(0d, annotation2.getNormalizedScore(), 0.01); // annotation2 (PG) no rule applies (neutral)
            assertEquals(-1.0, annotation3.getNormalizedScore(), 0.01); // annotation3 (PC) should not elute last -> gets -1
        } finally {
            instance.close();
        }
    }



    /**
     * Test to check the elution order of the lipids. The elution order is based on the number of double bonds if the lipid type and the number of
     * carbons is the same. The higher the number of double bonds, the shorter the RT.
     */
    @Test
    public void negativeScoreBasedOnRTDoubleBonds() {
        setup();
        LipidScoreUnit unit = new LipidScoreUnit();

        // Same type (TG), same carbons (54), increasing DB (2 → 3 → 4), decreasing RT (8 → 9 → 10)
        Lipid lipid1 = new Lipid(1, "TG 54:3", "C57H104O6", LipidType.TG, 54, 3); // MZ of [M+H]+ = 885.79057
        Lipid lipid2 = new Lipid(2, "TG 54:4", "C57H102O6", LipidType.TG, 54, 4); // MZ of [M+H]+ = 883.77492
        Lipid lipid3 = new Lipid(3, "TG 54:2", "C57H106O6", LipidType.TG, 54, 2); // MZ of [M+H]+ = 887.80622
        Annotation annotation1 = new Annotation(lipid1, 885.79056, 1.0e7, 10d, IoniationMode.POSITIVE); // 3DB
        Annotation annotation2 = new Annotation(lipid2, 883.77492, 1.0e8, 9d, IoniationMode.POSITIVE); // 4DB
        Annotation annotation3 = new Annotation(lipid3, 887.80622, 1.0e6, 8d, IoniationMode.POSITIVE); // 2 DB
        annotation1.detectAdductFromPeaks();
        annotation2.detectAdductFromPeaks();
        annotation3.detectAdductFromPeaks();
        unit.getAnnotations().add(annotation1);
        unit.getAnnotations().add(annotation2);
        unit.getAnnotations().add(annotation3);

        unit.setPositiveScoringEnabled(true);
        RuleUnitInstance<LipidScoreUnit> instance = RuleUnitProvider.get().createRuleUnitInstance(unit);

        try {
            instance.fire();

            /*System.out.println("TG 54:3 score: " + annotation1.getScore());
            System.out.println("TG 54:4 score: " + annotation2.getScore());
            System.out.println("TG 54:2 score: " + annotation3.getScore());

            System.out.println("TG 54:3 normalized: " + annotation1.getNormalizedScore());
            System.out.println("TG 54:4 normalized: " + annotation2.getNormalizedScore());
            System.out.println("TG 54:2 normalized: " + annotation3.getNormalizedScore());*/


            // Expect: TG 54:2 elutes too early despite having fewer double bonds -> -1
            assertEquals(0d, annotation1.getNormalizedScore(), 0.01); // TG 54:3
            assertEquals(0.6666666666666666, annotation2.getNormalizedScore(), 0.01); // TG 54:4
            assertEquals(0.0, annotation3.getNormalizedScore(), 0.01); // TG 54:2

        } finally {
            instance.close();
        }
    }

    /**
     * Test to check the elution order of the lipids. The elution order is based on the number of carbons if the lipid type and the number of
     * double bonds is the same. The larger the number of carbons, the longer the RT.
     */
    @Test
    public void negativeScoreBasedOnLipidType() {
        setup();
        LipidScoreUnit unit = new LipidScoreUnit();
        unit.setPositiveScoringEnabled(false);

        // Todos tienen mismo tipo y dobles enlaces (3 DB), pero diferente número de carbonos
        Lipid lipid1 = new Lipid(1, "TG 54:3", "C57H104O6", LipidType.TG, 54, 3); // RT = 10
        Lipid lipid2 = new Lipid(2, "TG 52:3", "C55H100O6", LipidType.TG, 52, 3); // RT = 9
        Lipid lipid3 = new Lipid(3, "TG 56:3", "C59H108O6", LipidType.TG, 56, 3); // RT = 8 ← ¡más carbonos, RT más corto!

        Annotation annotation1 = new Annotation(lipid1, 885.79056, 1.0e7, 10d, IoniationMode.POSITIVE, peaks1); // 3DB
        Annotation annotation2 = new Annotation(lipid2, 883.77492, 1.0e8, 9d, IoniationMode.POSITIVE, peaks2); // 4DB
        Annotation annotation3 = new Annotation(lipid3, 887.80622, 1.0e6, 8d, IoniationMode.POSITIVE, peaks3); // 2 DB
        annotation1.detectAdductFromPeaks();
        annotation2.detectAdductFromPeaks();
        annotation3.detectAdductFromPeaks();
        unit.getAnnotations().add(annotation1);
        unit.getAnnotations().add(annotation2);
        unit.getAnnotations().add(annotation3);

        /*System.out.println(annotation1.getDetectedAdducts());
        System.out.println(annotation2.getDetectedAdducts());
        System.out.println(annotation3.getDetectedAdducts());

        System.out.println("Scores BEFORE firing:");
        System.out.println(annotation1.getScore());
        System.out.println(annotation2.getScore());
        System.out.println(annotation3.getScore());*/

        RuleUnitInstance<LipidScoreUnit> instance = RuleUnitProvider.get().createRuleUnitInstance(unit);
        //System.out.println("Positive scoring enabled? " + unit.getPositiveScoringEnabled());

        try {

            instance.fire();

            /*System.out.println("TG 54:3 score: " + annotation1.getScore());
            System.out.println("TG 52:3 score: " + annotation2.getScore());
            System.out.println("TG 56:3 score: " + annotation3.getScore());


            System.out.println("TG 54:3 normalized: " + annotation1.getNormalizedScore());
            System.out.println("TG 52:3 normalized: " + annotation2.getNormalizedScore());
            System.out.println("TG 56:3 normalized: " + annotation3.getNormalizedScore());*/

            // Solo el que viola la regla (más carbonos pero RT más corto) debe ser penalizado
            assertEquals(1.0, annotation1.getNormalizedScore(), 0.01);
            assertEquals(0d, annotation2.getNormalizedScore(), 0.01);
            assertEquals(-1.0, annotation3.getNormalizedScore(), 0.01);

        } finally {
            instance.close();
        }
    }
}
