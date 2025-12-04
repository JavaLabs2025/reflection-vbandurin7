package org.example;

import org.example.classes.*;
import org.example.generator.Generator;

public class GenerateExample {
    public static void main(String[] args) {
        Generator gen = new Generator();


        try {
            System.out.println("1. Testing Cart generation:");
            Cart cart = (Cart) gen.generateValueOfType(Cart.class);
            System.out.println(cart);
            System.out.println();

            System.out.println("2. Testing Product generation:");
            Product product = (Product) gen.generateValueOfType(Product.class);
            System.out.println(product);
            System.out.println();

            System.out.println("3. Testing BinaryTreeNode generation:");
            BinaryTreeNode treeNode = (BinaryTreeNode) gen.generateValueOfType(BinaryTreeNode.class);
            System.out.println(treeNode);
            System.out.println();

            System.out.println("4. Testing Example generation:");
            Example example = (Example) gen.generateValueOfType(Example.class);
            System.out.println(example);
            System.out.println();

            System.out.println("5. Testing Rectangle generation:");
            Rectangle rectangle = (Rectangle) gen.generateValueOfType(Rectangle.class);
            System.out.println(rectangle);
            System.out.println();

            System.out.println("6. Testing Triangle generation:");
            Triangle triangle = (Triangle) gen.generateValueOfType(Triangle.class);
            System.out.println(triangle);
            System.out.println();

            System.out.println("7. Testing Shape generation (should pick implementation):");
            Shape shape = (Shape) gen.generateValueOfType(Shape.class);
            System.out.println(shape.getClass().getSimpleName() + ": " + shape);
            System.out.println();

            System.out.println("8. Generating multiple instances of each type:");

            System.out.println("\nMultiple Products:");
            for (int i = 0; i < 3; i++) {
                Product p = (Product) gen.generateValueOfType(Product.class);
                System.out.println("  " + p);
            }

            System.out.println("\nMultiple Examples:");
            for (int i = 0; i < 3; i++) {
                Example e = (Example) gen.generateValueOfType(Example.class);
                System.out.println("  " + e);
            }

        } catch (Throwable e) {
            System.err.println("Error during generation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}