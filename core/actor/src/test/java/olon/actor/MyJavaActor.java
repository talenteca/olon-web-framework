package olon.actor;


/**
 * Java implementation of LiftActor for test.
 */
public class MyJavaActor extends LiftActorJ {
    private int myValue = 0;

    @Receive protected void set(Set what) {
        myValue = what.num();
    }

    @Receive public void get(Get get) {
        reply(new Answer(myValue));
    }

    @Receive protected Answer add(Add toAdd) {
        myValue += toAdd.num();
        return new Answer(myValue);
    }

    @Receive public Answer sub(Sub toSub) {
        myValue -= toSub.num();
        return new Answer(myValue);
    }
}
