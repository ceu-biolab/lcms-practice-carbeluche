package adduct;

import java.util.Collections;
import java.util.LinkedHashMap; // Maintains insertion order
import java.util.Map;

/**
 * Utility Class to hold a map for positive adducts and another for negative ones
 */
public class AdductList {

    // Map<String, Double> maps an adduct name to a mass correction
    public static final Map<String, Double> MAPMZPOSITIVEADDUCTS;
    public static final Map<String, Double> MAPMZNEGATIVEADDUCTS;

    static {
        // In positive mode, the values are negative because they are added to m/z (adductMass is subtracted to recover the neutral mass)
        Map<String, Double> mapMZPositiveAdductsTMP = new LinkedHashMap<>();
        mapMZPositiveAdductsTMP.put("[M+H]+", -1.007276d);
        mapMZPositiveAdductsTMP.put("[M+2H]2+", -2.014552d);
        mapMZPositiveAdductsTMP.put("[M+Na]+", -22.989218d);
        mapMZPositiveAdductsTMP.put("[M+K]+", -38.963158d);
        mapMZPositiveAdductsTMP.put("[M+NH4]+", -18.033823d);
        mapMZPositiveAdductsTMP.put("[M+H-H2O]+", 17.0032d);
        mapMZPositiveAdductsTMP.put("[M+H+NH4]2+", -19.04165);
        mapMZPositiveAdductsTMP.put("[2M+H]+", -1.007276d);
        mapMZPositiveAdductsTMP.put("[2M+Na]+", -22.989218d);
        MAPMZPOSITIVEADDUCTS = Collections.unmodifiableMap(mapMZPositiveAdductsTMP); // maps made read-only

        // In negative mode, the adduct is added
        Map<String, Double> mapMZNegativeAdductsTMP = new LinkedHashMap<>();
        mapMZNegativeAdductsTMP.put("[M-H]−", 1.007276d);
        mapMZNegativeAdductsTMP.put("[M+Cl]−", -34.969402d);
        mapMZNegativeAdductsTMP.put("[M+HCOOH-H]−", -44.998201d);
        mapMZNegativeAdductsTMP.put("[M-H-H2O]−", 19.01839d);
        mapMZNegativeAdductsTMP.put("[2M-H]−", 1.007276d);
        mapMZNegativeAdductsTMP.put("[M-2H]2−", 1.007276d);
        MAPMZNEGATIVEADDUCTS = Collections.unmodifiableMap(mapMZNegativeAdductsTMP);
    }

}
