package org.example.classes;

import org.example.generator.Generatable;

@Generatable
public class Triangle implements Shape {
    private double sideA;
    private double sideB;
    private double sideC;

    public Triangle(double sideA, double sideB, double sideC) {
        this.sideA = sideA;
        this.sideB = sideB;
        this.sideC = sideC;
    }

    @Override
    public double getArea() {
        double s = (sideA + sideB + sideC) / 2;
        return Math.sqrt(s * (s - sideA) * (s - sideB) * (s - sideC));
    }

    @Override
    public double getPerimeter() {
        return sideA + sideB + sideC;
    }

    @Override
    public String toString() {
        return "Triangle{" +
                "sides=[" + String.format("%.2f", sideA) +
                ", " + String.format("%.2f", sideB) +
                ", " + String.format("%.2f", sideC) + "]" +
                ", area=" + String.format("%.2f", getArea()) +
                ", perimeter=" + String.format("%.2f", getPerimeter()) +
                '}';
    }
}