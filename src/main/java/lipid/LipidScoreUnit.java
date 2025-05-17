package lipid;

import org.drools.ruleunits.api.DataSource;
import org.drools.ruleunits.api.DataStore;
import org.drools.ruleunits.api.RuleUnitData;
import java.util.HashSet;

/**
 * Class that serves as a Rule Unit - a container that holds the data (facts) that Drools rules will process
 */
public class LipidScoreUnit implements RuleUnitData {

    private final DataStore<Annotation> annotations; // Facts saved as objects of type Annotation
    private boolean positiveScoringEnabled;

    /**
     * Default constructor: to create a new empty DatStore<Annotation>
     */
    public LipidScoreUnit() {
        this(DataSource.createStore()); // DataSource.createStore() creates a new DataStore<Annotation>, that then is passed as argument to the second constructor
    }

    /**
     * Constructor for injecting an existing DataStore<Annotations>
     * @param annotations existing previously
     */
    public LipidScoreUnit(DataStore<Annotation> annotations) {
        this.annotations = annotations;
    }
    /**
     * Provides access to the DataStore so that Drools can evaluate and modify the facts
     * Drools Rule Units automatically discover facts by calling getX() methods.
     * Rules can reference /annotations in .drl files thanks to this method.
     */
    public DataStore<Annotation> getAnnotations() {
        return annotations;
    }

    public boolean getPositiveScoringEnabled() {
        return positiveScoringEnabled;
    }


    public void setPositiveScoringEnabled(boolean positiveScoringEnabled) {
        this.positiveScoringEnabled = positiveScoringEnabled;
    }

}


