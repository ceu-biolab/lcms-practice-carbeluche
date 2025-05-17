package lipid;

import java.util.Arrays;
import java.util.List;

/**
 * Auxiliar Class for Types of Lipids ordering of elution
 */
public class LipidUtils {

    /**
     * To define the elution order externally of the glycerophospholipid elution hierarchy
     * PG < PE < PI < PA < PS << PC
     */
    private static final List<LipidType> ELUTION_ORDER = Arrays.asList(
            LipidType.PG,
            LipidType.PE,
            LipidType.PI,
            LipidType.PC,
            LipidType.PA,
            LipidType.PS
    );

    /**
     * Compare two LipidType values based on predefined elution order
     * @param type1
     * @param type2
     * @return -1 (when type1 elutes before type2), 0 (if same), 1 (when type1 elutes after type2)
     */
    public static int compareLipidTypes (LipidType type1, LipidType type2){
        int index1 = ELUTION_ORDER.indexOf(type1);
        int index2 = ELUTION_ORDER.indexOf(type2);

        if(index1 == -1 && index2 == -1) return 0; // if both types are unknown, they are equal
        if(index1 == -1) return 1; // type1 is unknown, so type1 elutes after
        if(index2 == -1) return -1; // type2 is unknown, so type 1 elutes before
        // known types are prioritized: TG elutes after everything else by default

        return Integer.compare(index1, index2);
    }

    public static boolean elutesBefore(LipidType type1, LipidType type2){
        return compareLipidTypes(type1, type2)<0;
    }

    public static boolean elutesAfter(LipidType type1, LipidType type2){
        return compareLipidTypes(type1, type2)>0;
    }

    public static boolean elutesSame (LipidType type1, LipidType type2){
        return compareLipidTypes(type1, type2)==0;
    }

    public static boolean violatesCarbonElutionOrder(Lipid l1, Lipid l2, double rt1, double rt2) {
        return l1.getLipidType() == l2.getLipidType() &&
                l1.getDoubleBonds() == l2.getDoubleBonds() &&
                l1.getCarbons() > l2.getCarbons() &&
                rt1 < rt2;
    }

}
