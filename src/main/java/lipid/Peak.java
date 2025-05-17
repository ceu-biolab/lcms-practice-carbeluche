package lipid;

/**
 * Represents a single MS peak.
 * Peaks can be sorted by m/z because Peak Class implements Comparable
 */
public class Peak implements Comparable<Peak>{
    private final double mz;
    private final double intensity;

    public Peak(double mz, double intensity) {
        this.mz = mz;
        this.intensity = intensity;
    }

    public double getMz() {
        return mz;
    }

    public double getIntensity() {
        return intensity;
    }

    @Override
    public String toString() {
        return String.format("Peak(mz=%.4f, intensity=%.2f)", mz, intensity);
    }

    /**
     * hashCode() when storing objects in hash-based collections like HashSet/HashMap
     * Must match equals()
     * @return
     */
    @Override
    public int hashCode() {
        return Double.hashCode(mz) * 31; // Double.hasCode(mz) turns the double m/z into a integer hash
        // Then, multiplying by 31 reduces hash collisions (prime number)
    }

    /**
     * Two peaks are equal if their mz values are equal
     * Intensity does not affect the object identity
     * @param obj Peak
     * @return true or false if they are equal or not, respectively
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Peak)) return false;
        Peak other = (Peak) obj;
        return Double.compare(mz, other.mz) == 0;
    }

    /**
     * Peaks naturally ordered automatically: sorted by increasing m/z value required by TreeSet<Peak> and Collections.sort(List<Peak>)
     * @param other the object to be compared.
     * @return <0 (negative if mz comes before), 0 (same position) or >0 (positive if mz comes after)
     */
    @Override
    public int compareTo (Peak other){
        return Double.compare(this.mz, other.mz);
    }

    // When two Peak objects have exactly the same mz: Set.of() is an immutable set that does not guarantee the insertion order
    // TreeSet<>(Set.of(...)) may use unexpected ordering and reject duplicates silently
}
