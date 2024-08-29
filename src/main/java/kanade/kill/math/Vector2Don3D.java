package kanade.kill.math;

public class Vector2Don3D extends Vector {

    public Vector2Don3D(Vector vector) {
        this(vector.start.clone(), vector.start.add(vector.XLength(), 0, vector.ZLength(), false));
    }

    public Vector2Don3D(BlockPos start, BlockPos end) {
        super(start, end);
    }

    public Vector2Don3D(BlockPos start, double distance, double degree) {
        this.start = start;
        double rad = Math.toRadians(degree);
        double X = distance * Math.cos(rad);
        double Z = distance * Math.sin(rad);
        this.end = start.add(X, 0, Z, false);
    }
}
