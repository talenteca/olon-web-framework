package olon.http;

/**
 * The Java class that has LiftRules instance
 * and LiftRules-related Helper methods
 */
public class LiftRulesJ {
    public static LiftRules j() {
	return (new LiftRulesJBridge()).liftRules();
    }
}