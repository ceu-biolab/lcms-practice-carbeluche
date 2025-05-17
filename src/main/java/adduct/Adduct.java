package adduct;

import lipid.IoniationMode;

/**
 * Adduct class handles MS adduct transformations
 * @author carbeluche
 */
public class Adduct {

    /**
     * Extract the adduct mass shift given the adduct name
     *
     * @param adduct name
     *
     * @return the adduct mass shift in Daltons, or null
     */
    private static Double getAdductMass (String adduct){
        if(adduct == null){
            return null;
        }
        Double adductMass = AdductList.MAPMZPOSITIVEADDUCTS.get(adduct);
        if(adductMass == null){
            adductMass = AdductList.MAPMZNEGATIVEADDUCTS.get(adduct);
        }
        return adductMass;
    }

    /**
     * Parse the multimer count and charge from an adduct string
     * Examples: [2M+H]+ has multimer = 2 and charge = 1, and [M+2H]2+ has multimer = 1 and charge = 2
     *
     * @param adduct name
     *
     * @return [multimer, charge], with default [1,1]
     */
    private static int[] parseMultimerAndCharge (String adduct){
        int multimer=1;
        int charge=1;
        if(adduct == null || !adduct.startsWith("[") || !adduct.endsWith("]")){
            return new int[]{multimer, charge};
        }
        try{
            String inner = adduct.substring(1, adduct.length()-1); // Removes [ and ]
            if(inner.charAt(0)>='0' && inner.charAt(0)<= '9'){
                int mIndex = inner.indexOf('M');
                if(mIndex>0){
                    String multimeterString = inner.substring(0, mIndex);
                    multimer = Integer.parseInt(multimeterString); // Searches for a number before M
                }
            }
            // looks backward from the end of the string to find a digit that represents the charge
            for(int i=inner.length()-1; i>=0; i--){
                if(Character.isDigit(inner.charAt(i))){
                    int end=i+1;
                    int start=i;
                    while(start > 0 && Character.isDigit(inner.charAt(start-1))){
                        start--;
                    }
                    String chargeString = inner.substring(start, end);
                    if(!chargeString.isEmpty()){
                        charge = Integer.parseInt(chargeString);
                    }
                    break;
                }

            }
        }catch(Exception e){
            // {1,1} by default
        }
        return new int[]{multimer, charge};
    }

    /**
     * Whether the adduct is negative ion mode (lost proton or gained a negative ion)
     *
     * @param adduct name
     *
     * @return IoniationMode POSITIVE or NEGATIVE
     */
    private static IoniationMode getIoniationMode (String adduct){
        if(adduct==null){
            return IoniationMode.POSITIVE;
        }
        if(adduct.contains("−") || adduct.contains("-")){ // If it contains "-" unicode or "-" keyboard
            return IoniationMode.NEGATIVE;
        }else{
            return IoniationMode.POSITIVE;
        }
    }


    /**
     * Calculate the mass to search depending on the adduct hypothesis
     *
     * @param mz mz
     * @param adduct adduct name
     *
     * @return the monoisotopic mass of the experimental mass mz with the adduct @param adduct
     */
    public static Double getMonoisotopicMassFromMZ(Double mz, String adduct) {
        if((mz ==null)||(adduct == null)){
            return null;
        }
        Double adductMass = getAdductMass(adduct);
        if(adductMass == null){
            return null;
        }
        int [] parsed = parseMultimerAndCharge(adduct);
        int multimer = parsed[0];
        int charge = parsed[1];
        IoniationMode mode = getIoniationMode(adduct);
        double adjustedMZ;
        // monoisotopic mass = (mz-adductMass) x charge
        if(mode == IoniationMode.NEGATIVE){
            adjustedMZ = mz + adductMass; // In negative mode, adducts are lost, so subtracting a negative = adding
        }else{
            adjustedMZ= mz - adductMass; // In positive mode, adducts are added, so subtract them to go back to neutral
        }
        double massToSearch = adjustedMZ*charge;
        massToSearch=massToSearch/multimer;
        return massToSearch;
    }

    /**
     * Calculate the mz of a monoisotopic mass with the corresponding adduct
     *
     * @param monoisotopicMass of the neutral molecule
     * @param adduct adduct name
     *
     * @return the computed m/z value
     */
    public static Double getMZFromMonoisotopicMass(Double monoisotopicMass, String adduct) {
        if(monoisotopicMass == null || adduct ==null){
            return null;
        }
        Double adductMass=getAdductMass(adduct);
        if(adductMass == null){
            return null;
        }
        int[] parsed = parseMultimerAndCharge(adduct);
        int multimer = parsed[0];
        int charge = parsed[1];
        IoniationMode mode = getIoniationMode(adduct);
        Double massToSearch = monoisotopicMass * multimer;
        if(charge > 1){
            massToSearch = massToSearch / charge; // MZ = ( (monoisotopicMass x multimer)/charge ) +- adductMass
        }
        Double MZ;
        if(mode == IoniationMode.NEGATIVE){
            MZ = massToSearch - adductMass; // In negative mode, adduct is lost
        } else{
            MZ = massToSearch + adductMass; // In positive mode, adduct is added
        }
        return MZ;
    }

    /**
     * Returns the ppm difference between measured mass and theoretical mass
     *
     * @param experimentalMass    Mass measured by MS
     * @param theoreticalMass Theoretical mass of the compound
     *
     * @return the absolute difference in ppm as |(exp-theo)/the|x10^6
     */
    public static int calculatePPMIncrement(Double experimentalMass, Double theoreticalMass) {
        int ppmIncrement;
        ppmIncrement = (int) Math.round(Math.abs((experimentalMass - theoreticalMass) * 1000000
                / theoreticalMass));
        return ppmIncrement;
    }

    /**
     * Returns the absolute mass difference of the tolerance in ppm given based on a given experimental mass
     *
     * @param experimentalMass    Mass measured by MS
     * @param ppm ppm of tolerance
     *
     * @return the absolute mass error (delta) in Daltons
     */
    public static double calculateDeltaPPM(Double experimentalMass, int ppm) {
        double deltaPPM;
        deltaPPM =  Math.abs(Math.abs((experimentalMass * ppm) / 1_000_000.0 )); // DeltaPPM = (mass x ppm)/1.000.000
        // /1000000 converts from parts per million to an absolute number
        // 1_000_000.0 ensures floating-point division not to truncate decimals
        // Math.round() rounds the result to the nearest whole number
        return deltaPPM;
    }

}
