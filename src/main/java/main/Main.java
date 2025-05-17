package main;

import lipid.*;
import org.drools.ruleunits.api.RuleUnitInstance;
import org.drools.ruleunits.api.RuleUnitProvider;
import java.util.List;
import java.util.Set;

public class Main {

    public static void main(String[] args) {
        LipidScoreUnit lipidScoreUnit = new LipidScoreUnit(); // Creation of the Rule Unit container
        // Build Annotation instances with grouped Peaks
        Peak mH = new Peak (700.500, 100000.0); // [M+H]+
        Peak mNa = new Peak (722.482, 80000.0); // [M+Na]+
        Lipid lipid = new Lipid(1, "PC 34:1", "C42H82NO8P", LipidType.PC, 34, 1);
        Annotation annotation = new Annotation(lipid, 700.49999, 80000.0, 6.5d, IoniationMode.POSITIVE, Set.of(mH, mNa));
        // Insert them into the unit
        lipidScoreUnit.getAnnotations().add(annotation);
        // Creation of a running instance that loads all .drl rules associated to evaluate facts (Annotation)
        RuleUnitInstance<LipidScoreUnit> instance = RuleUnitProvider.get().createRuleUnitInstance(lipidScoreUnit);

        try {
            instance.fire(); // To trigger the rules in lipids.drl
            System.out.println("Detected adduct: " +annotation.getAdduct());
        } finally {
            instance.close(); // To clean up memory/resources
        }
    }
}
