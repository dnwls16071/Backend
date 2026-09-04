package ch6;

public class Rectangle implements Shape{

    private Point topLeft;
    private double height;
    private double width;

    @Override
    public double getArea() {
        return height * width;
    }
}
