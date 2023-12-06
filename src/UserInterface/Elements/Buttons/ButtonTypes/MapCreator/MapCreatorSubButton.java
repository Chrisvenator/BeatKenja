package UserInterface.Elements.Buttons.ButtonTypes.MapCreator;

import UserInterface.Elements.Buttons.ButtonType;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.Exceptions.TookTooLongException;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.Buttons.MySubButton;

public class MapCreatorSubButton extends MySubButton {
    public MapCreatorSubButton(ButtonType button, MyButton parent) {
        super(button, parent);
    }

    //TODO: exception handling

    protected void printException(Exception e) {
        String errorMessage = "Unknown Error!";
        if (e instanceof NullPointerException) errorMessage = "NullPointerException!";
        if (e instanceof TookTooLongException) errorMessage = "An infinite loop occurred! Please try again.";
        else errorMessage = e.getMessage();

        printErrorMessage(e, errorMessage);
    }

    //DO NOT QUESTION THIS SECTION
    //IT WAS NECESSARY TO ENSURE THAT THERE IS NO INFINITE LOOP

    /**
     * This method watches over a thread and interrupts it if it takes too long.
     * In essence, it prevents infinite loops.
     * @param thread The thread to watch over
     */
    protected void watchOverThread(Thread thread) {
        Thread watchForInfiniteLoop = new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            thread.interrupt();
            throw new IllegalArgumentException("Took too long lol");
        });

        try {
            watchForInfiniteLoop.start();
            thread.start();
            watchForInfiniteLoop.interrupt();
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage().contains("Took too long lol")) printException(new TookTooLongException("Took too long lol"));
            else printException(ex);
        }
    }
}
