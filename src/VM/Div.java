package VM;

public class Div implements Instruction {
    private final int offsetLeft;
    private final int offsetRight;
    private final int destination;

    Div(int offsetLeft, int offsetRight, int destination) {
        this.destination = destination;
        this.offsetLeft = offsetLeft;
        this.offsetRight = offsetRight;
    }

    @Override
    public void execute(VmRuntime runtime) {
        runtime.setStackAt(this.destination, runtime.stackAt(this.offsetLeft) / runtime.stackAt(this.offsetRight));
    }

    @Override
    public void println() {
        System.out.println("DIV");
    }
}
