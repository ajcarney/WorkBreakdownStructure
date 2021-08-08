import javafx.application.Application;
import javafx.stage.Stage;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * Main application class for the WorkBreakdownStructure. Starts gui and manages exceptions
 * http://tutorials.jenkov.com/javafx/treetableview.html
 *
 * @author Aiden Carney
 */
public class Main extends Application {

    private static ArrayList<String> cliArgs = new ArrayList<>();

    /**
     * Starts the gui application
     *
     * @param primaryStage the Stage object which acts as the main window
     */
    @Override
    public void start(Stage primaryStage) {

    }


    /**
     * Default error handler. Called by java whenever there is an error that is not handled. Prints a message
     * to the terminal, logs the info about the error including the stacktrace,
     * and saves all open files to a recovery spot in case it is a bad error
     *
     * @param t the current thread
     * @param e the unhandled error
     */
    private static void handleError(Thread t, Throwable e) {
        System.err.println("***An unhandled exception was thrown***");
        System.err.println("An unexpected error occurred in " + t);
        System.err.println(e.getMessage());
        System.err.println("Check the log file for information");
        System.err.println("Saving files to .recovery\n\n");
        System.err.println(e.getStackTrace());
        e.printStackTrace();

        File logDir = new File("./.log");
        File logFile = new File("./.log/log");
        if(!logDir.exists()) logDir.mkdir();

        try {
            Writer w = new FileWriter(logFile, true);  // open file in append mode

            Date date = new Date();  // write time stamp
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a");
            String formattedDate = sdf.format(date);
            w.write("[" + formattedDate + "]\n");
            w.write(e.getMessage() + "\n");

            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            w.write(sw.toString() + "\n");

            w.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

//        File recoveryDir = new File("./.recovery");
//        if(!recoveryDir.exists()) recoveryDir.mkdir();
//        for(Map.Entry<Integer, DSMData> matrix : matrixHandler.getMatrices().entrySet()) {
//            File f = new File("./.recovery/" + matrixHandler.getMatrixSaveFile(matrix.getKey()).getName());
//            ExportHandler.saveMatrixToFile(matrix.getValue(), f);
//            matrix.getValue().setWasModified();  // matrix is not saved to known location, so don't display it as saved to the user
//        }

    }


    /**
     * starts the application
     *
     * @param args any command line args used by javafx (probably not used anywhere and will be ignored)
     */
    public static void main(String[] args) {
        for(int i=0; i<args.length; i++) {
            cliArgs.add(args[i]);
        }

        launch(args);  // starts gui application
    }


}
