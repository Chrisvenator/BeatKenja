package CustomWaveGenerator;

public record Coordinate(float x, double y) {
    @Override
    public String toString() {
        return "x=" + x + ", y=" + y;
    }
}
