import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.layout.*;

import java.io.File;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * This program uses the file included in the repository to plot a 3-D shape, in this case a cube, and project it onto
 * a 2-D JavaFX WritableImage. This program is also capable of modifying the original projection by means of translating
 * it, scaling it, and rotating it in any of the three dimensions. The shape is always viewed from the same point,
 * (6, 8, 7.5) in space, and is projected to be viewed from 60 centimeters away.
 *
 * Note: Go to line 326 and change the pathname of the file to the new pathname before executing the program
 *
 * @author Nitin Chennam
 * @version 1.1
 */
public class PerspectiveProjection extends Application {

    File file;
    ArrayList<Integer> points;
    int[][] pointArray;
    Text space = new Text("");
    ArrayList<Integer> pointToPoint = new ArrayList<>();
    int[] projectedPoints;
    WritableImage image;
    float viewpointX, viewpointY, viewpointZ;
    int viewPlaneZ = -60;

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * This method is the main driver class. It contains all of the necessary components for the rest of the program to
     * execute.
     * @param primaryStage
     */
    @Override
    public void start(Stage primaryStage) {

        VBox root = new VBox();
        Scene rootScene = new Scene(root);
        Text welcome = new Text("Welcome to the 3D Projection App");
        Button originalProjection = new Button("See original Projection");
        Button transformProjection = new Button("Transform Original Projection");

        root.getChildren().addAll(welcome, originalProjection, space, transformProjection);
        root.setAlignment(Pos.CENTER);

        originalProjection.setOnAction(e -> {
            createPointArrayList();
            viewpointX = 6;
            viewpointY = 8;
            viewpointZ = 7.5F;
            Integer temp[] = new Integer[]{0, 1, 1, 2, 2, 3, 3, 0, 4, 5, 5, 6, 6, 7, 7, 4, 0, 6, 1, 7, 2, 4, 3, 5};
            ArrayList<Integer> temp1 = new ArrayList<>();
            temp1.addAll(Arrays.asList(temp));
            pointToPoint = temp1;
            createFinalArray();
            writeToImage();
            displayImage();
            //projection();
        });

        transformProjection.setOnAction(e -> {
            createPointArrayList();
            viewpointX = 6;
            viewpointY = 8;
            viewpointZ = 7.5F;
            Integer temp[] = new Integer[]{0, 1, 1, 2, 2, 3, 3, 0, 4, 5, 5, 6, 6, 7, 7, 4, 0, 6, 1, 7, 2, 4, 3, 5};
            ArrayList<Integer> temp1 = new ArrayList<>();
            temp1.addAll(Arrays.asList(temp));
            pointToPoint = temp1;
            transform();
        });

        primaryStage.setScene(rootScene);
        primaryStage.show();

    }

    /**
     * This method is the root for the rest of the program. It contains options for the user to either view the original
     * projection of the image provided, or to modify the image
     */
    private void transform(){
        Button rotate = new Button("Rotate");
        Button translate = new Button("Translate");
        Button scale = new Button("Scale");
        Button back = new Button("Back");
        Button display = new Button("Display");
        VBox transformRoot = new VBox();
        Scene transformScene = new Scene(transformRoot);
        Stage transformStage = new Stage();
        TextField xText = new TextField("X: ");
        TextField yText = new TextField("Y: ");
        TextField zText = new TextField("Z: ");
        TextField xScale = new TextField("X Scale: ");
        TextField yScale = new TextField("Y Scale: ");
        TextField zScale = new TextField("Z Scale: ");
        TextField xTranslate = new TextField("X Translate");
        TextField yTranslate = new TextField("Y Translate");
        TextField zTranslate = new TextField("Z Translate");
        TextField angle = new TextField("Angle: ");

        transformRoot.getChildren().addAll(translate, scale, rotate);

        translate.setOnAction(e -> {
            transformRoot.getChildren().removeAll(scale, rotate);
            transformRoot.getChildren().addAll(xTranslate, yTranslate, zTranslate, display, back);
            back.setOnAction(b -> {
                transformRoot.getChildren().removeAll(xTranslate, yTranslate, zTranslate, xText, yText, zText, display, back);
                transformRoot.getChildren().addAll(scale, rotate);
            });

            display.setOnAction(d -> {
            ThreeDTranslate(Integer.parseInt(xTranslate.getText()), Integer.parseInt(yTranslate.getText()), Integer.parseInt(zTranslate.getText()));
            createFinalArray();
            System.out.println(Arrays.toString(projectedPoints));
            writeToImage();
            displayImage();
            });
        });

        scale.setOnAction(e -> {
            transformRoot.getChildren().removeAll(translate, rotate);
            transformRoot.getChildren().addAll(xScale, yScale, zScale, xText, yText, zText, display, back);

            back.setOnAction(b -> {
                transformRoot.getChildren().removeAll(xScale, yScale, zScale, xText, yText, zText, display, back);
                transformRoot.getChildren().addAll(translate, rotate);
            });

            display.setOnAction(d -> {
               ThreeDScale(Integer.parseInt(xScale.getText()), Integer.parseInt(yScale.getText()), Integer.parseInt(zScale.getText()), Integer.parseInt(xText.getText()), Integer.parseInt(yText.getText()), Integer.parseInt(zText.getText()));
               createFinalArray();
               writeToImage();
               displayImage();
            });

        });

        rotate.setOnAction(e -> {
            transformRoot.getChildren().removeAll(translate, scale);
            transformRoot.getChildren().addAll(angle, xText, yText, zText, display, back);

            back.setOnAction(b -> {
                transformRoot.getChildren().removeAll(angle, display, back);
                transformRoot.getChildren().addAll(translate, scale);
            });

            display.setOnAction(d -> {
                ThreeDRotate(Integer.parseInt(angle.getText()));
                createFinalArray();
                writeToImage();
                displayImage();
            });
        });

        transformStage.setScene(transformScene);
        transformStage.show();
    }

    /**
     * This is the method the executes the translation of the 3D points that were provided by the file. It takes three
     * parameters, x, y, and z, which designate the amount that the image is shifted in each direction
     * @param x The amount that the image is shifted in the X direction
     * @param y The amount that the image is shifted in the Y direction
     * @param z The amount that the image is shifted in the Z direction
     */
    private void ThreeDTranslate(int x, int y, int z){
        ArrayList<Integer> temp = new ArrayList<>();
        for(int i = 0; i < pointArray.length; i++){

            pointArray[i][0] = pointArray[i][0] - x;
            pointArray[i][1] = pointArray[i][1] - y;
            pointArray[i][2] = pointArray[i][2] - z;

        }//for

    }
    /**
     * This is the method the executes the translation of the 3D points that were provided by the file. It takes three
     * parameters, x, y, and z, which designate the amount that the image is scaled in each direction
     * @param x The amount that the image is scaled in the X direction
     * @param y The amount that the image is scaled in the Y direction
     * @param z The amount that the image is scaled in the Z direction
     */
    private void ThreeDScale(int x, int y, int z, int pointX, int pointY, int pointZ){

        ThreeDTranslate(-pointX, -pointY, -pointZ);

        for(int i = 0; i < pointArray.length; i++) {
            int[] matrix1 = new int[]{pointArray[i][0], pointArray[i][1], pointArray[i][2], 1};
            int[][] matrix2 = new int[][]{{x, 0, 0, 0}, {0, y, 0, 0}, {0, 0, z, 0}, {0, 0, 0, 1}};


            int[] initialReturnMatrix = multiplyMatrix(matrix1, matrix2);
            int[] returnMatrix = new int[] {initialReturnMatrix[0], initialReturnMatrix[1], initialReturnMatrix[2]};
            pointArray[i] = returnMatrix;
        }

        ThreeDTranslate(pointX, pointY, pointZ);

    }
    /**
     * This is the method the executes the translation of the 3D points that were provided by the file. It takes three
     * parameters, x, y, and z, which designate the amount that the image is scaled in each direction.
     *
     * Note: This method is not fully functional. It rotates each of the lines instead of the entire image
     *
     * @param degrees The number of degrees that the image is rotated around the Z axis
     */
    private void ThreeDRotate(int degrees){
        double angle = convertToRadians(degrees);
        for(int i = 0; i < pointArray.length; i++){
            int[] matrix1 = new int[] {pointArray[i][0], pointArray[i][1], pointArray[i][2], 1};
            double[][] matrix2 = new double [][]{{Math.cos(angle), -1 * Math.sin(angle), 0, 0},
                    {Math.sin(angle), Math.cos(angle), 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}};

            double[] initialReturnMatrix = multiplyMatrix(matrix1, matrix2);
            int[] initialReturnMatrix0 = new int[] {(int)Math.round(initialReturnMatrix[0]), (int)Math.round(initialReturnMatrix[1]), (int)Math.round(initialReturnMatrix[2]), 1};
            int[] returnMatrix = new int[] {initialReturnMatrix0[0], initialReturnMatrix0[1], initialReturnMatrix0[2]};

            pointArray[i] = returnMatrix;
        }
//        double angle = convertToRadians(angle0);
//
//        for(int i = 0; i < points.length/2; i++) {
//            int[] matrix1 = new int[] {points[i * 2], points[i * 2 + 1], 1};
//            double[][] matrix2 = new double[][] {{Math.cos(angle), -1 * Math.sin(angle), 0},
//                    {Math.sin(angle), Math.cos(angle), 0}, {0, 0, 1}};
//
//            System.out.println(Arrays.toString(matrix2[0]));
//            System.out.println(Arrays.toString(matrix2[1]));
//            System.out.println(Arrays.toString(matrix2[2]));
//
//            double[] initialReturnMatrix = multiplyMatrix(matrix1, matrix2);
//            int[] initialReturnMatrix0 = new int[] {(int)Math.round(initialReturnMatrix[0]), (int)Math.round(initialReturnMatrix[1]), 1};
//            int[] returnMatrix = new int[] {initialReturnMatrix0[0], initialReturnMatrix0[1]};
//            if (i > 0)
//                finalDataArray = appendArrays(finalDataArray, returnMatrix);
//            else
//                finalDataArray = returnMatrix;
//        }
    }

    /**
     * An unused method that can be used to allow the user to implement their own file and corresponding 3D shape.
     * It is not implemented to allow for easier testing.
     */
    private void projection(){
        VBox projectionRoot = new VBox();
        HBox line00 = new HBox();
        TextField viewPointX = new TextField("ViewPoint X: ");
        TextField viewPointY = new TextField("ViewPoint Y: ");
        TextField viewPointZ = new TextField("ViewPoint Z: ");
        TextField startPoint = new TextField("Enter start point(1-12)");
        TextField endPoint = new TextField("Enter end point(1-12)");
        Button display = new Button("Display");

        Scene projectionScene = new Scene(projectionRoot);
        Stage projectionStage = new Stage();

        Text instructions = new Text("Enter the start and end points for all visible lines \n Press enter to go to the next line \n Click the \"Continue\" button below to display the image");
        Text to = new Text("\t to \t");

        line00.getChildren().addAll(startPoint, to, endPoint);
        projectionRoot.getChildren().addAll(instructions, viewPointX, viewPointY, viewPointZ, space, line00, display);

        viewPointZ.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode() == KeyCode.ENTER){
                    viewpointX = Float.parseFloat(viewPointX.getText());
                    viewpointY = Float.parseFloat(viewPointY.getText());
                    viewpointZ = Float.parseFloat(viewPointZ.getText());
                    startPoint.requestFocus();
                }
            }
        });

        endPoint.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if(keyEvent.getCode() == KeyCode.ENTER){
                    pointToPoint.add(Integer.parseInt(startPoint.getText()) - 1);
                    pointToPoint.add(Integer.parseInt(endPoint.getText()) - 1);
                    startPoint.clear();
                    endPoint.clear();
                    startPoint.requestFocus();
                }
            }
        });

        display.setOnAction(e -> {
            createFinalArray();
            writeToImage();
            displayImage();
        });

        projectionStage.setScene(projectionScene);
        projectionStage.show();
    }//projection()

    /**
     * Creates the ArrayList of the 3D points that make up the shape in question
     */
    private void createPointArrayList(){
        this.file = new File("/Users/nitin_c/Desktop/projection.txt");
        points = new ArrayList<>();
        try {
            Scanner scan = new Scanner(this.file);
            while(scan.hasNext()){
                points.add(Integer.parseInt(scan.next()));
            }//while
        }//try
        catch(Exception e){
            e.printStackTrace();
        }//catch
        int arrayLength = points.size() / 3;
        pointArray = new int[arrayLength][3];
        for(int i = 0; i < arrayLength; i++){
            for(int j = 0; j < 3; j++){
                pointArray[i][j] = points.get(i*3+j);
            }
        }

    }//createPointArrayList()

    /**
     * This method uses a line-drawing algorithm to write the lines between the provided points to the WritableImage.
     */
    private void writeToImage(){
        image = new WritableImage(1920, 1080);
        PixelWriter pixelWriter = image.getPixelWriter();
        Color color = Color.BLACK;
        for(int i = 0; i < projectedPoints.length/4; i++) {

            int startX = projectedPoints[i*4];
            int startY = projectedPoints[i*4+1];
            int endX = projectedPoints[i*4+2];
            int endY = projectedPoints[i*4+3];
            double x = startX;
            double y = startY;
            double slope = getM(startX, endX, startY, endY);
            if (Math.abs(slope) < 1) {
                int beginX;
                int finishX;
                if(startX < endX) {
                    beginX = startX;
                    finishX = endX;
                }
                else{
                    beginX = endX;
                    finishX = startX;
                }
                for (x = beginX; x <= finishX; x++) {
                    pixelWriter.setColor((int) x + 200, (int) Math.round(y) + 200, color);
                    y += slope;
                }//for
            }//slope < 1

            else {
                int beginY;
                int finishY;
                if(startY < endY) {
                    beginY = startY;
                    finishY = endY;
                }
                else{
                    beginY = endY;
                    finishY = startY;
                }
                for (y = beginY; y <= finishY; y++) {
                    pixelWriter.setColor((int) x + 200, (int) Math.round(y) + 200, color);
                    x += 1 / slope;
                }//for
            }// slope > 1
        }
    }//writeToImage()

    /**
     * This method created a stage and a scene to place the WritableImage onto and displays the image
     */
    private void displayImage(){
        VBox viewRoot = new VBox();
        ImageView imageView = new ImageView(image);
        viewRoot.getChildren().add(imageView);
        Scene displayScene = new Scene(viewRoot,
                1920, 1080);

        Stage viewStage = new Stage();
        viewStage.setScene(displayScene);
        viewStage.show();
    }

    /**
     * A very simple method that simply returns the slope between the provided points
     * @param startX The X coordinate of the start point
     * @param endX The X coordinate of the end point
     * @param startY The Y coordinate of the start point
     * @param endY The Y coordinate of the end point
     * @return The slope
     */
    private double getM(int startX, int endX, int startY, int endY){
        double slope = ((double)endY - (double)startY)/((double)endX - (double)startX);
        return slope;
    }

    /**
     * This method converts the 3-D points provided by the file to 2D points that can be displayed on the image
     */
    private void createFinalArray(){

        ArrayList<Integer> temp = new ArrayList<>();
        for(int i = 0; i < pointToPoint.size(); i++){
            int tempIndex = pointToPoint.get(i);

            int x = pointArray[tempIndex][0];
            int y = pointArray[tempIndex][1];
            int z = pointArray[tempIndex][2];
            int newX = (int)(x*((viewpointZ - viewPlaneZ)/(viewpointZ - z)) + viewpointX*((viewPlaneZ - z)/(viewpointZ - z)));
            int newY = (int)(y*((viewpointZ - viewPlaneZ)/(viewpointZ - z)) + viewpointY*((viewPlaneZ - z)/(viewpointZ - z)));

            temp.add(newX);
            temp.add(newY);
        }//for

        projectedPoints = new int[temp.size()];
        for(int i = 0; i < temp.size(); i++){
            projectedPoints[i] = temp.get(i) + 100;
        }
    }//convertPoints()

    /**
     * This method multiplies two matrices using matrix multiplication
     * @param m1 the first matrix
     * @param m2 the second matrix
     * @return a 1D integer array that contains the new set of points
     */
    private int[] multiplyMatrix(int[] m1, int [][] m2){
        int[] result = new int [m2[0].length];

        for (int j = 0; j < m2[0].length; j++) {
            for (int k = 0; k < m1.length; k++) {
                result[j] += m1[k] * m2[k][j];
            }
        }
        return result;
    }
    /**
     * This method multiplies two matrices using matrix multiplication
     * @param m1 the first matrix
     * @param m2 the second matrix
     * @return a 1D double array that contains the new set of points
     */
    private double[] multiplyMatrix(int[] m1, double [][] m2){
        double[] result = new double[m2[0].length];
        for (int j = 0; j < m2[0].length; j++) {
            for (int k = 0; k < m1.length; k++) {
                System.out.println(m1[k] * m2[k][j]);
                result[j] += m1[k] * m2[j][k];
            }
            System.out.println();
            System.out.println(result[j]);
        }
        return result;
    }

    /**
     * A simple method that converts degrees to radians
     * @param degrees the number of degrees
     * @return the degree value in radians
     */
    private double convertToRadians(int degrees){
        double result = degrees * (Math.PI / 180);
        return result;
    }

}
