package cis501;

public interface IDirectionPredictor {

    /**
     * @param pc the program counter of the uop needing prediction
     * @return the predicted direction for the uop
     */
    public Direction predict(long pc);

    /**
     * @param pc     the program counter of the branch to train
     * @param actual the true direction of the branch
     */
    public void train(long pc, Direction actual);

}
