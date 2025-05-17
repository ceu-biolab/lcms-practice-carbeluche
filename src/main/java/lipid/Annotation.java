package lipid;

import adduct.Adduct;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Class to represent the annotation over a lipid
 */
public class Annotation {
    private final Lipid lipid;
    private final double mz;
    private final double intensity; // The intensity of the most abundant peak in the groupedPeaks
    private final double rtMin;
    private final IoniationMode ionizationMode;
    private String adduct; // The adduct will be detected based on the groupedSignals
    private final Set<Peak> groupedSignals;
    private int score;
    private int totalScoresApplied;
    private final Set<String> detectedAdducts = new HashSet<>();
    private boolean hasBeenScored = false;

    private boolean scoreAssigned = false;


    /**
     * @param lipid
     * @param mz
     * @param intensity
     * @param retentionTime
     * @param ionizationMode
     */
    public Annotation(Lipid lipid, double mz, double intensity, double retentionTime, IoniationMode ionizationMode) {
        this(lipid, mz, intensity, retentionTime, ionizationMode, Collections.emptySet());
    }

    /**
     * @param lipid
     * @param mz
     * @param intensity
     * @param retentionTime
     * @param ionizationMode
     * @param groupedSignals
     */
    public Annotation(Lipid lipid, double mz, double intensity, double retentionTime, IoniationMode ionizationMode, Set<Peak> groupedSignals) {
        this.lipid = lipid;
        this.mz = mz;
        this.rtMin = retentionTime;
        this.intensity = intensity;
        this.ionizationMode = ionizationMode;
        // Sorted by m/z to facilitate deisotoping (ignore the peaks that represent isotopes) and adduct detection
        this.groupedSignals = new TreeSet<>(groupedSignals); // TreeSet creates an automatically ordered set
        // The lowest peak is the principal adduct
        this.score = 0;
        this.totalScoresApplied = 0;
    }

    public boolean isHasBeenScored(){
        return hasBeenScored;
    }

    public void setHasBeenScored(boolean hasBeenScored){
        this.hasBeenScored=hasBeenScored;
    }
    public Set<String> getDetectedAdducts(){
        return Collections.unmodifiableSet(detectedAdducts);
    }

    public Lipid getLipid() {
        return lipid;
    }

    public double getMz() {
        return mz;
    }

    public double getRtMin() {
        return rtMin;
    }

    public String getAdduct() {
        return adduct;
    }

    public void setAdduct(String adduct) {
        this.adduct = adduct;
    }

    public double getIntensity() {
        return intensity;
    }

    public IoniationMode getIonizationMode() {
        return ionizationMode;
    }

    public Set<Peak> groupedSignals() {
        return Collections.unmodifiableSet(groupedSignals);
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
        this.scoreAssigned=true;
    }
    public boolean isScoreAssigned(){
        return scoreAssigned;
    }
    public int getTotalScoreApplied(){
        return totalScoresApplied;
    }

    /**
     * @param delta must be [-1,1]
     */
    public void addScore(int delta) {
        if(delta>1) delta=1;
        if(delta<-1) delta=-1;
        this.score += delta;
        this.totalScoresApplied++; // sums always 1 to totalScoresApplied even if delta=0
    }

    /**
     * @return The normalized score between -1 and 1, being the average of the applied scores
     */
    public double getNormalizedScore() {
        if(totalScoresApplied == 0) return 0.0; // To avoid division by 0
        double normalized = (double) score / totalScoresApplied;
        return Math.max(-1.0, Math.min(1.0, normalized)); // To be inside [-1,1]
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Annotation)) return false;
        Annotation that = (Annotation) o;
        return Double.compare(that.mz, mz) == 0 &&
                Double.compare(that.rtMin, rtMin) == 0 &&
                Objects.equals(lipid, that.lipid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lipid, mz, rtMin);
    }

    @Override
    public String toString() {
        return String.format("Annotation(%s, mz=%.4f, RT=%.2f, adduct=%s, intensity=%.1f, score=%d)",
                lipid.getName(), mz, rtMin, adduct, intensity, score);
    }

    /**
     * Given a group of ordered peaks and a ppm tolerance
     * @param orderedPeaks by m/z
     * @param ppmTolerance to identify is two mass are near enough to be considered isotopes (approx 1.00335Da)
     * @return peaks not isotopes in order they have appeared
     */
    public Set<Peak> deisotopePeaks (Set<Peak> orderedPeaks, int ppmTolerance){
        List<Peak> sorted = new ArrayList<>(orderedPeaks); // Set converted into List
        Set<Peak> filtered = new LinkedHashSet<>(); // Peaks without isotopes
        for(int i=0; i<sorted.size(); i++){
            Peak current = sorted.get(i); // Initially assumes that current is not an isotope
            boolean isIsotope = false;
            for(int j=i+1; j<sorted.size();j++){
                Peak candidate = sorted.get(j); // Compares the current peak with all peaks that followed (j>i)
                double deltaMZ = candidate.getMz()- current.getMz(); // Real difference between masses
                double deltaAllowed = Adduct.calculateDeltaPPM(current.getMz(), ppmTolerance); // Margin permitted to consider something is "near" (absolute tolerance in Da)
                if(Math.abs(deltaMZ-1.00335) <= deltaAllowed && candidate.getIntensity()< current.getIntensity()){
                    isIsotope=true; // If masses difference is 1.00335Da and the next candidate peak has lower intensity than the current, then candidate is an isotope of current
                    break;
                }
            }
            if(!isIsotope){
                filtered.add(current); // Save if current peak is not an isotope
            }
        }
        return filtered;
    }

    public void detectAdductFromPeaks(){
        // Ensure there must be grouped peaks
        if( groupedSignals != null && !groupedSignals.isEmpty() ){
           //1. Deisotope signals with 5ppm tolerance
            Set<Peak> cleanSignals = deisotopePeaks(groupedSignals, 5);
            //2. Get the peak with highest m/z -> [H+M]+ lowest
            Peak base = null;
            for(Peak p:cleanSignals){
                if(base==null || p.getMz() > base.getMz()){
                    base=p;
                }
            }
            if(base!=null){
                double baseMZ = base.getMz(); // If base peak is found, obtain the m/z value
                // Known adducts
                double protonMass = 1.0073; // +H = [M+H]+
                double sodiumMass = 22.9898; //+Na = [M+Na]+
                double waterLoss = 18.0106; //-H2O = water loss
                double twoProtons = 2.0146; // +2H = adduct that is doubled charged
                for(Peak other: cleanSignals){
                    if (Math.abs(base.getMz() - other.getMz()) < 0.001) continue; // See the other peaks to compare with the base peak
                    double delta = Math.abs(other.getMz()-baseMZ); // actual m/z difference
                    double tolerance = Adduct.calculateDeltaPPM(baseMZ,10);
                    // [M+Na]+ - [M+H]+ = 21.9825
                    if(Math.abs(delta-(sodiumMass-protonMass)) <= tolerance){
                        detectedAdducts.add("[M+Na]+");
                        this.adduct= "[M+H]+"; // If +Na - +H = 21.9825Da then base adduct is [M+H]+
                        return;
                    }
                    // [M+H]+ - [M+H-H20]+ = 18.0106
                    if(Math.abs(delta-waterLoss) <= tolerance){
                        detectedAdducts.add(("[M+H-H2O]+"));
                        this.adduct= "[M+H]+"; // other is [M+H-H2O]+ and [M+H]+ is the base peak
                        return;
                    }
                    // calculate rounded expected m/z for [M+2H]2+
                    double expectedDoubleChargeMz = roundMz((baseMZ + protonMass) / 2,5);
                    // round the observed peak
                    double observedMz= roundMz(other.getMz(),5);
                    double deltaDouble = Math.abs(observedMz - expectedDoubleChargeMz);
                    double toleranceDouble = Adduct.calculateDeltaPPM(expectedDoubleChargeMz, 15);
                    //System.out.printf("Comparing for [M+2H]2+: base=%.5f, other=%.5f, expected=%.5f, delta=%.5f, tol=%.5f%n",
                     //       baseMZ, other.getMz(), expectedDoubleChargeMz, deltaDouble, toleranceDouble);
                    // apply threshold
                    if (deltaDouble <= toleranceDouble) {
                        this.detectedAdducts.add("[M+2H]2+");
                        this.adduct = "[M+H]+";
                        return;

                    }
                }
                System.out.println("No known adducts detected for " + lipid.getName() + ", defaulting to [M+H]+");
                this.adduct = "[M+H]+";

            }
        }

    }

    private static double roundMz(double mz, int decimal) {
        // Solves that 457.41465 internally converts into 457.41464999999997
        BigDecimal bd = BigDecimal.valueOf(mz);
        bd= bd.setScale(decimal, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public void winComparison() {
        this.score++;
        this.totalScoresApplied++;
    }

    public void loseComparison() {
        this.totalScoresApplied++;
    }

    public boolean scoreAlreadySet() {
        return totalScoresApplied > 0 || score != 0;
    }


}
