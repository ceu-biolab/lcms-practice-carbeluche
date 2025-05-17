package adduct;

/**
 * Utility Class for mass transformations in MS
 */
public class MassTransformation {

    /**
     * Convert an experimental m/z value to a mono isotopic mass using the given adduct
     *
     * @param mz measured
     * @param adduct name
     *
     * @return the monoisotopic mass
     */
    public static Double mzToMonoisotopicMass(Double mz, String adduct){
        return Adduct.getMonoisotopicMassFromMZ(mz,adduct);
    }

    /**
     * Convert a mono isotopic mass to an expected m/z value using the given adduct
     *
     * @param monoisotopicMass the neutral mass
     * @param adduct
     *
     * @return the m/z value
     */
    public static Double monoisotopicMassToMz (Double monoisotopicMass, String adduct){
        return Adduct.getMZFromMonoisotopicMass(monoisotopicMass, adduct);
    }

}
