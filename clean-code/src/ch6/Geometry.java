package ch6;

public class Geometry {
    private final double PI = 3.141592653589793;

    public double area(Object shape) {
        if (shape instanceof Square) {
            Square s = (Square) shape;
            return s.side * s.side;
        }

        if (shape instanceof Circle) {
            Circle c = (Circle) shape;
            return PI * c.radius * c.radius;
        }

        throw new IllegalArgumentException("알 수 없는 도형입니다.");
    }
}
